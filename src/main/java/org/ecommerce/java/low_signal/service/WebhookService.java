package org.ecommerce.java.low_signal.service;

import java.nio.charset.StandardCharsets;

import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.ecommerce.java.low_signal.model.StripeWebhookEvent;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.repository.StripeWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.model.Event;

import jakarta.transaction.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Ricezione degli eventi Stripe.
 *
 * Tre difese, in ordine:
 * 1. firma verificata sull'eschibyte del corpo (nessun evento firmato male entra);
 * 2. idempotenza tramite event_id (Stripe puo' rinviare lo stesso evento);
 * 3. lo stato dell'ordine viene aggiornato solo per gli eventi che interessano.
 *
 * Il corpo viene parsato con Jackson invece che con i tipi del SDK: i webhook
 * usano la versione API del tuo account, che puo' non coincidere con quella
 * della libreria (in quel caso l'OGGETTO deserIALIZZATO dal SDK viene scartato).
 */
@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final ObjectMapper objectMapper;
    private final StripeGateway stripeGateway;
    private final OrderRepository orderRepository;
    private final StripeWebhookEventRepository eventRepository;
    private final OrderService orderService;
    private final String webhookSecret;

    public WebhookService(ObjectMapper objectMapper,
            StripeGateway stripeGateway,
            OrderRepository orderRepository,
            StripeWebhookEventRepository eventRepository,
            OrderService orderService,
            @Value("${stripe.webhook-secret:${STRIPE_WEBHOOK_SECRET:}}") String webhookSecret) {
        this.objectMapper = objectMapper;
        this.stripeGateway = stripeGateway;
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
        this.orderService = orderService;
        this.webhookSecret = webhookSecret;
    }

    /**
     * @param payload        corpo esatto della richiesta (byte)
     * @param signatureHeader header Stripe-Signature
     * @throws PaymentException 401 se la firma non e' valida
     */
    @Transactional
    public void handleEvent(byte[] payload, String signatureHeader) {
        String body = new String(payload, StandardCharsets.UTF_8);

        // 1. Firma: se non e' Stripe a mandarla, esce da qui con 401
        Event event = stripeGateway.verifyWebhook(body, signatureHeader, webhookSecret);

        String eventId = event.getId();
        if (eventId == null || eventId.isBlank()) {
            throw PaymentException.badRequest("Evento Stripe senza id");
        }

        // 2. Idempotenza: evento gia' ricevuto e processato
        if (eventRepository.existsById(eventId)) {
            log.info("Evento Stripe {} gia' processato, duplicato ignorato", eventId);
            return;
        }

        JsonNode objectNode = parseObjectNode(body);
        String paymentIntentId = objectNode.path("id").asText(null);
        String objectType = objectNode.path("object").asText("");

        StripeWebhookEvent record = new StripeWebhookEvent();
        record.setEventId(eventId);
        record.setEventType(event.getType());
        record.setPaymentIntentId(paymentIntentId);
        // saveAndFlush: se un evento identico arriva in parallelo, il vincolo
        // sulla chiave primaria lo blocca prima del processing
        eventRepository.saveAndFlush(record);

        if (!"payment_intent".equals(objectType)) {
            log.info("Evento Stripe '{}' con oggetto '{}' ignorato", event.getType(), objectType);
            return;
        }

        processPaymentIntentEvent(event.getType(), paymentIntentId);
    }

    private void processPaymentIntentEvent(String eventType, String paymentIntentId) {
        if (eventType == null) {
            return;
        }
        switch (eventType) {
            case "payment_intent.succeeded" -> handleSucceeded(paymentIntentId);
            case "payment_intent.payment_failed" -> handleFailed(paymentIntentId);
            case "payment_intent.canceled" -> handleCanceled(paymentIntentId);
            default -> log.debug("Evento Stripe '{}' ignorato", eventType);
        }
    }

    private void handleSucceeded(String paymentIntentId) {
        Order order = findOrderByIntent(paymentIntentId);
        if (order == null) {
            log.warn("Nessun ordine collegato all'intent {}", paymentIntentId);
            return;
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        if (order.getPaymentStatus() == PaymentStatus.CANCELED) {
            // I soldi sono stati presi: l'ordine deve diventare PAID per forza
            log.error("L'ordine {} era CANCELED ma l'intent {} e' riuscito: controllare lo stock",
                    order.getOrderNumber(), paymentIntentId);
        }
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
        log.info("Ordine {} pagato con successo (intent {})",
                order.getOrderNumber(), paymentIntentId);
    }

    private void handleFailed(String paymentIntentId) {
        Order order = findOrderByIntent(paymentIntentId);
        if (order == null || order.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderService.releaseStock(order);
        orderRepository.save(order);
        log.info("Pagamento fallito per l'ordine {}: stock rilasciato",
                order.getOrderNumber());
    }

    private void handleCanceled(String paymentIntentId) {
        Order order = findOrderByIntent(paymentIntentId);
        if (order == null || order.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }
        order.setPaymentStatus(PaymentStatus.CANCELED);
        orderService.releaseStock(order);
        orderRepository.save(order);
        log.info("Pagamento annullato per l'ordine {}: stock rilasciato",
                order.getOrderNumber());
    }

    private Order findOrderByIntent(String paymentIntentId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return null;
        }
        return orderRepository.findByStripePaymentIntent(paymentIntentId).orElse(null);
    }

    private JsonNode parseObjectNode(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return root.path("data").path("object");
        } catch (Exception e) {
            throw PaymentException.badRequest("Corpo del webhook non leggibile");
        }
    }
}
