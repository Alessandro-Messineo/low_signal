package org.ecommerce.java.low_signal.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ecommerce.java.low_signal.dto.CreatePaymentIntentRequest;
import org.ecommerce.java.low_signal.dto.PaymentIntentResponse;
import org.ecommerce.java.low_signal.dto.PaymentStatusResponse;
import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.service.StripeGateway.PaymentIntentData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Ciclo di vita del pagamento di un ordine.
 *
 * Regole:
 * - l'importo viene SEMPRE letto dal database (order.totalPrice), mai dal client;
 * - lo stato definitivo arriva dal webhook, GET /status serve solo ad
 *   aggiornare subito l'interfaccia al ritorno dal pagamento;
 * - lo stock viene rilasciato solo a pagamento fallito/annullato o alla scadenza.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    /**
     * Stati Stripe per i quali l'intent e' ancora pagabile e puo' essere riutilizzato.
     * Attenzione: Stripe non ha uno stato "failed": dopo un pagamento rifiutato
     * l'intent torna in "requires_payment_method".
     */
    private static final Set<String> PAYABLE_STATUSES = Set.of(
            "requires_payment_method",
            "requires_confirmation",
            "requires_action",
            "processing");

    /** Importo minimo accettato da Stripe per l'euro (in centesimi). */
    private static final long MIN_AMOUNT_CENTS = 50L;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final StripeGateway stripeGateway;
    private final String currency;
    private final int expiryMinutes;

    public PaymentService(OrderRepository orderRepository,
            OrderService orderService,
            StripeGateway stripeGateway,
            @Value("${stripe.currency:${STRIPE_CURRENCY:eur}}") String currency,
            @Value("${app.payment.expiry-minutes:30}") int expiryMinutes) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.stripeGateway = stripeGateway;
        this.currency = currency;
        this.expiryMinutes = expiryMinutes;
    }

    /**
     * Crea (o riusa) il PaymentIntent per un ordine PENDING dell'utente.
     */
    @Transactional
    public PaymentIntentResponse createIntent(Integer userId, CreatePaymentIntentRequest request) {
        Order order = loadOwnedOrder(userId, request.getOrderId());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw PaymentException.conflict(
                    "Order " + order.getOrderNumber() + " is already paid");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw PaymentException.conflict(
                    "Order " + order.getOrderNumber() + " can no longer be paid (status: "
                            + order.getPaymentStatus() + "). Create a new order.");
        }

        long amountCents = toCents(order.getTotalPrice());

        // Riusa l'intent gia' creato: evita intent duplicati sul doppio click
        String existingIntentId = order.getStripePaymentIntent();
        if (existingIntentId != null && !existingIntentId.isBlank()) {
            PaymentIntentData existing = retrieveQuietly(existingIntentId);
            if (existing != null) {
                // Allinea l'ordine con Stripe: se il pagamento e' gia' riuscito ma
                // il webhook non e' ancora arrivato, NON se ne crea un altro
                applyStripeStatus(order, existing.status());

                if (order.getPaymentStatus() == PaymentStatus.PAID) {
                    orderRepository.save(order);
                    throw PaymentException.conflict(
                            "Order " + order.getOrderNumber() + " is already paid");
                }
                if (order.getPaymentStatus() != PaymentStatus.PENDING) {
                    orderRepository.save(order);
                    throw PaymentException.conflict(
                            "Order " + order.getOrderNumber() + " can no longer be paid (status: "
                                    + order.getPaymentStatus() + "). Create a new order.");
                }
                if (PAYABLE_STATUSES.contains(existing.status())) {
                    log.info("Riuso PaymentIntent {} per l'ordine {}", existing.id(), order.getOrderNumber());
                    orderRepository.save(order);
                    return buildResponse(order, existing, amountCents);
                }
            }
        }

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("orderId", String.valueOf(order.getId()));
        metadata.put("orderNumber", order.getOrderNumber());
        metadata.put("userId", String.valueOf(order.getUser().getId()));

        // Idempotenza: la stessa chiave restituisce lo stesso intent
        String idempotencyKey = "order_" + order.getId() + "_payment_intent";
        PaymentIntentData intent = stripeGateway.createPaymentIntent(
                amountCents, currency, metadata, idempotencyKey);

        order.setStripePaymentIntent(intent.id());
        orderRepository.save(order);

        log.info("PaymentIntent {} creato per l'ordine {} ({} {})",
                intent.id(), order.getOrderNumber(), amountCents, currency);

        return buildResponse(order, intent, amountCents);
    }

    /**
     * Stato dell'ordine + stato reale interrogato su Stripe.
     * Usato al ritorno dal pagamento: il webhook arriva con un po' di ritardo.
     */
    @Transactional
    public PaymentStatusResponse getStatus(Integer userId, Integer orderId) {
        Order order = loadOwnedOrder(userId, orderId);

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setPaymentIntentId(order.getStripePaymentIntent());

        String intentId = order.getStripePaymentIntent();
        if (intentId == null || intentId.isBlank()) {
            response.setStripeStatus("none");
            return response;
        }

        PaymentIntentData intent = stripeGateway.retrievePaymentIntent(intentId);
        response.setStripeStatus(intent.status());

        applyStripeStatus(order, intent.status());
        orderRepository.save(order);
        response.setPaymentStatus(order.getPaymentStatus().name());

        return response;
    }

    /**
     * Annulla gli ordini PENDING piu' vecchi di {@code expiryMinutes} e ne
     * rilascia lo stock: senza questo gli ordini non pagati bloccherebbero
     * il magazzino in modo permanente.
     *
     * @return numero di ordini annullati
     */
    @Transactional
    public int releaseExpiredOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(expiryMinutes);
        List<Order> expired = orderRepository.findByPaymentStatusAndCreatedAtBefore(
                PaymentStatus.PENDING, cutoff);

        for (Order order : expired) {
            cancelStripeIntent(order);
            order.setPaymentStatus(PaymentStatus.CANCELED);
            orderService.releaseStock(order);
            orderRepository.save(order);
            log.info("Ordine {} scaduto dopo {} minuti: annullato e stock rilasciato",
                    order.getOrderNumber(), expiryMinutes);
        }
        return expired.size();
    }

    /**
     * Allinea lo stato dell'ordine con lo stato Stripe.
     * Non degrada mai uno stato gia' assegnato (FAILED/CANCELED rimangono tali).
     */
    void applyStripeStatus(Order order, String stripeStatus) {
        if (stripeStatus == null) {
            return;
        }
        switch (stripeStatus) {
            case "succeeded" -> {
                if (order.getPaymentStatus() != PaymentStatus.PAID) {
                    if (order.getPaymentStatus() == PaymentStatus.CANCELED) {
                        log.error("L'ordine {} era CANCELED ma il pagamento {} e' riuscito: controllare lo stock",
                                order.getOrderNumber(), order.getStripePaymentIntent());
                    }
                    order.setPaymentStatus(PaymentStatus.PAID);
                    log.info("Ordine {} pagato (intent {})",
                            order.getOrderNumber(), order.getStripePaymentIntent());
                }
            }
            case "canceled" -> {
                if (order.getPaymentStatus() == PaymentStatus.PENDING) {
                    order.setPaymentStatus(PaymentStatus.CANCELED);
                    orderService.releaseStock(order);
                    log.info("Pagamento annullato per l'ordine {}: stock rilasciato",
                            order.getOrderNumber());
                }
            }
            default -> {
                // requires_payment_method / requires_confirmation / processing:
                // il pagamento non e' concluso, l'ordine resta in attesa
            }
        }
    }

    private Order loadOwnedOrder(Integer userId, Integer orderId) {
        if (orderId == null) {
            throw PaymentException.badRequest("Order id is required");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> PaymentException.notFound(
                        "Order not found with id: " + orderId));

        User owner = order.getUser();
        if (owner == null || !owner.getId().equals(userId)) {
            throw PaymentException.forbidden("Order does not belong to the current user");
        }
        return order;
    }

    private PaymentIntentData retrieveQuietly(String paymentIntentId) {
        try {
            return stripeGateway.retrievePaymentIntent(paymentIntentId);
        } catch (PaymentException e) {
            log.warn("Intent {} non recuperabile ({}): ne viene creato uno nuovo",
                    paymentIntentId, e.getMessage());
            return null;
        }
    }

    private void cancelStripeIntent(Order order) {
        String intentId = order.getStripePaymentIntent();
        if (intentId == null || intentId.isBlank()) {
            return;
        }
        try {
            stripeGateway.cancelPaymentIntent(intentId);
        } catch (PaymentException e) {
            log.warn("Intent {} non annullabile per l'ordine {}: {}",
                    intentId, order.getOrderNumber(), e.getMessage());
        }
    }

    private PaymentIntentResponse buildResponse(Order order, PaymentIntentData intent, long amountCents) {
        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setPaymentIntentId(intent.id());
        response.setClientSecret(intent.clientSecret());
        response.setStripeStatus(intent.status());
        response.setPaymentStatus(order.getPaymentStatus().name());
        response.setAmountCents(amountCents);
        response.setCurrency(currency);
        return response;
    }

    /**
     * Converte l'importo BigDecimal (scala 2) in centesimi interi.
     * Nessun double: evita errori di arrotondamento sul denaro.
     */
    private long toCents(BigDecimal amount) {
        if (amount == null) {
            throw PaymentException.badRequest("Order has no amount");
        }
        long cents;
        try {
            cents = amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw PaymentException.badRequest("Invalid order amount: " + amount.toPlainString());
        }
        if (cents < MIN_AMOUNT_CENTS) {
            throw PaymentException.badRequest(
                    "Order amount is too low for Stripe (minimum 0.50 EUR)");
        }
        return cents;
    }
}
