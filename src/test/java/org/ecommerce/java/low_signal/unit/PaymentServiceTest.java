package org.ecommerce.java.low_signal.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ecommerce.java.low_signal.dto.CreatePaymentIntentRequest;
import org.ecommerce.java.low_signal.dto.PaymentIntentResponse;
import org.ecommerce.java.low_signal.dto.PaymentStatusResponse;
import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.service.OrderService;
import org.ecommerce.java.low_signal.service.PaymentService;
import org.ecommerce.java.low_signal.service.StripeGateway;
import org.ecommerce.java.low_signal.service.StripeGateway.PaymentIntentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private StripeGateway stripeGateway;

    private PaymentService paymentService;

    private User user;
    private Order order;

    @BeforeEach
    void setUp() {
        // Costruzione manuale: il servizio legge la valuta e la scadenza da @Value
        paymentService = new PaymentService(orderRepository, orderService, stripeGateway, "eur", 30);

        user = new User();
        user.setId(1);
        user.setEmail("mario@email.com");

        order = new Order();
        order.setId(7);
        order.setOrderNumber("LS-123");
        order.setTotalPrice(new BigDecimal("153.99"));
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
    }

    private CreatePaymentIntentRequest request(Integer orderId) {
        CreatePaymentIntentRequest request = new CreatePaymentIntentRequest();
        request.setOrderId(orderId);
        return request;
    }

    @Test
    void createIntent_ordinePending_CreaIntentConImportoDalDatabase() {
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.createPaymentIntent(anyLong(), anyString(), anyMap(), anyString()))
                .thenReturn(new PaymentIntentData("pi_1", "requires_payment_method", "sec_1", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntentResponse response = paymentService.createIntent(1, request(7));

        assertNotNull(response);
        assertEquals("sec_1", response.getClientSecret());
        assertEquals("pi_1", response.getPaymentIntentId());
        // 153.99 EUR -> 15399 centesimi, mai calcolato dal client
        assertEquals(15399L, response.getAmountCents());
        assertEquals("eur", response.getCurrency());
        assertEquals("pi_1", order.getStripePaymentIntent());
        verify(orderRepository).save(order);
    }

    @Test
    void createIntent_metadataContengonoOrdineEUtente() {
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.createPaymentIntent(anyLong(), anyString(), anyMap(), anyString()))
                .thenReturn(new PaymentIntentData("pi_1", "requires_payment_method", "sec_1", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.createIntent(1, request(7));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> metadata = ArgumentCaptor.forClass(Map.class);
        verify(stripeGateway).createPaymentIntent(anyLong(), eq("eur"), metadata.capture(), anyString());
        assertEquals("7", metadata.getValue().get("orderId"));
        assertEquals("LS-123", metadata.getValue().get("orderNumber"));
        assertEquals("1", metadata.getValue().get("userId"));
    }

    @Test
    void createIntent_ordineDiUnAltroUtente_Vietato() {
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(999, request(7)));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        verify(stripeGateway, never()).createPaymentIntent(anyLong(), anyString(), anyMap(), anyString());
    }

    @Test
    void createIntent_ordineInesistente_NonTrovato() {
        when(orderRepository.findById(42)).thenReturn(Optional.empty());

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(42)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void createIntent_ordineGiaPagato_Conflitto() {
        order.setPaymentStatus(PaymentStatus.PAID);
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(7)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(stripeGateway, never()).createPaymentIntent(anyLong(), anyString(), anyMap(), anyString());
    }

    @Test
    void createIntent_ordineFallito_Conflitto_StockGiaRilasciato() {
        order.setPaymentStatus(PaymentStatus.FAILED);
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(7)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("can no longer be paid"));
    }

    @Test
    void createIntent_intentEsistentePagabile_VieneRiusato() {
        order.setStripePaymentIntent("pi_vecchio");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.retrievePaymentIntent("pi_vecchio"))
                .thenReturn(new PaymentIntentData("pi_vecchio", "requires_payment_method", "sec_vecchia", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentIntentResponse response = paymentService.createIntent(1, request(7));

        assertEquals("pi_vecchio", response.getPaymentIntentId());
        assertEquals("sec_vecchia", response.getClientSecret());
        verify(stripeGateway, never()).createPaymentIntent(anyLong(), anyString(), anyMap(), anyString());
    }

    @Test
    void createIntent_intentEsistenteGiaRiuscito_MaiUnSecondoAddebito() {
        // Caso peggiore: il cliente ha pagato ma il webhook non e' ancora arrivato
        order.setStripePaymentIntent("pi_gia_pagato");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.retrievePaymentIntent("pi_gia_pagato"))
                .thenReturn(new PaymentIntentData("pi_gia_pagato", "succeeded", "sec_x", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(7)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("already paid"));
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        // Nessun nuovo PaymentIntent: nessun rischio di doppio addebito
        verify(stripeGateway, never()).createPaymentIntent(anyLong(), anyString(), anyMap(), anyString());
    }

    @Test
    void createIntent_intentEsistenteAnnullato_StockRilasciatoEAncoraConflitto() {
        order.setStripePaymentIntent("pi_annullato");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.retrievePaymentIntent("pi_annullato"))
                .thenReturn(new PaymentIntentData("pi_annullato", "canceled", "sec_x", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(7)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertEquals(PaymentStatus.CANCELED, order.getPaymentStatus());
        verify(orderService).releaseStock(order);
        verify(stripeGateway, never()).createPaymentIntent(anyLong(), anyString(), anyMap(), anyString());
    }

    @Test
    void createIntent_troppoPocoPerStripe_Vietato() {
        order.setTotalPrice(new BigDecimal("0.10"));
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> paymentService.createIntent(1, request(7)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void getStatus_pagamentoRiuscito_OrdineDiventaPaid() {
        order.setStripePaymentIntent("pi_1");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.retrievePaymentIntent("pi_1"))
                .thenReturn(new PaymentIntentData("pi_1", "succeeded", "sec_1", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentStatusResponse response = paymentService.getStatus(1, 7);

        assertEquals("PAID", response.getPaymentStatus());
        assertEquals("succeeded", response.getStripeStatus());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        // Un pagamento riuscito non tocca mai il magazzino
        verify(orderService, never()).releaseStock(any(Order.class));
    }

    @Test
    void getStatus_intentAnnullato_OrdineAnnullatoEStockRilasciato() {
        order.setStripePaymentIntent("pi_1");
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));
        when(stripeGateway.retrievePaymentIntent("pi_1"))
                .thenReturn(new PaymentIntentData("pi_1", "canceled", "sec_1", 15399L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentStatusResponse response = paymentService.getStatus(1, 7);

        assertEquals("CANCELED", response.getPaymentStatus());
        verify(orderService).releaseStock(order);
    }

    @Test
    void getStatus_senzaIntent_RitornaStatoDellOrdine() {
        when(orderRepository.findById(7)).thenReturn(Optional.of(order));

        PaymentStatusResponse response = paymentService.getStatus(1, 7);

        assertEquals("PENDING", response.getPaymentStatus());
        assertEquals("none", response.getStripeStatus());
        verify(stripeGateway, never()).retrievePaymentIntent(anyString());
    }

    @Test
    void releaseExpiredOrders_OrdineScaduto_AnnullatoEStockRilasciato() {
        order.setCreatedAt(LocalDateTime.now().minusHours(2));
        order.setStripePaymentIntent("pi_scaduto");
        when(orderRepository.findByPaymentStatusAndCreatedAtBefore(eq(PaymentStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        int released = paymentService.releaseExpiredOrders();

        assertEquals(1, released);
        assertEquals(PaymentStatus.CANCELED, order.getPaymentStatus());
        verify(orderService).releaseStock(order);
        // L'intent viene annullato su Stripe: nessuno puo' piu' pagarlo
        verify(stripeGateway).cancelPaymentIntent("pi_scaduto");
    }
}
