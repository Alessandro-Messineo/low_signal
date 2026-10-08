package org.ecommerce.java.low_signal.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.ecommerce.java.low_signal.model.StripeWebhookEvent;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.OrderRepository;
import org.ecommerce.java.low_signal.repository.StripeWebhookEventRepository;
import org.ecommerce.java.low_signal.service.OrderService;
import org.ecommerce.java.low_signal.service.WebhookService;
import org.ecommerce.java.low_signal.service.StripeGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.stripe.model.Event;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Mock
    private StripeGateway stripeGateway;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StripeWebhookEventRepository eventRepository;

    @Mock
    private OrderService orderService;

    private WebhookService webhookService;

    private Order order;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(new ObjectMapper(), stripeGateway, orderRepository,
                eventRepository, orderService, WEBHOOK_SECRET);

        User user = new User();
        user.setId(1);

        order = new Order();
        order.setId(7);
        order.setOrderNumber("LS-123");
        order.setTotalPrice(new BigDecimal("153.99"));
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStripePaymentIntent("pi_1");
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
    }

    private String payload(String type) {
        return payload("evt_1", type, "pi_1", "payment_intent");
    }

    private String payload(String eventId, String type, String objectId, String objectType) {
        return "{\"id\":\"" + eventId + "\",\"type\":\"" + type + "\",\"data\":{\"object\":"
                + "{\"id\":\"" + objectId + "\",\"object\":\"" + objectType + "\"}}}";
    }

    private byte[] bytes(String payload) {
        return payload.getBytes(StandardCharsets.UTF_8);
    }

    private void acceptSignature() {
        acceptSignature("payment_intent.succeeded");
    }

    /** La firma viene verificata e l'evento restituito dal gateway e' quello del payload. */
    private void acceptSignature(String eventType) {
        when(stripeGateway.verifyWebhook(anyString(), anyString(), anyString()))
                .thenReturn(event("evt_1", eventType));
    }

    private Event event(String id, String type) {
        Event event = new Event();
        event.setId(id);
        event.setType(type);
        return event;
    }

    @Test
    void firmaNonValida_VieneRifiutata() {
        when(stripeGateway.verifyWebhook(anyString(), anyString(), anyString()))
                .thenThrow(PaymentException.unauthorized("Firma del webhook non valida"));

        PaymentException ex = assertThrows(PaymentException.class,
                () -> webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x"));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        verify(eventRepository, never()).saveAndFlush(any(StripeWebhookEvent.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void payloadVienePassatoIntattoAllaVerificaDellaFirma() {
        String body = payload("payment_intent.succeeded");
        when(stripeGateway.verifyWebhook(anyString(), anyString(), eq(WEBHOOK_SECRET)))
                .thenReturn(event("evt_1", "payment_intent.succeeded"));
        when(eventRepository.existsById("evt_1")).thenReturn(true);

        webhookService.handleEvent(bytes(body), "t=1,v1=x");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(stripeGateway).verifyWebhook(captor.capture(), anyString(), anyString());
        assertEquals(body, captor.getValue());
    }

    @Test
    void eventoGiaRicevuto_VieneIgnorato() {
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(true);

        assertDoesNotThrow(() -> webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x"));

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderService, never()).releaseStock(any(Order.class));
    }

    @Test
    void paymentIntentSucceeded_OrdineDiventaPaid() {
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.of(order));

        webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x");

        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(orderRepository).save(order);
        verify(orderService, never()).releaseStock(any(Order.class));
        verify(eventRepository).saveAndFlush(any(StripeWebhookEvent.class));
    }

    @Test
    void paymentIntentFailed_OrdineFallitoEStockRilasciato() {
        acceptSignature("payment_intent.payment_failed");
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.of(order));

        webhookService.handleEvent(bytes(payload("payment_intent.payment_failed")), "t=1,v1=x");

        assertEquals(PaymentStatus.FAILED, order.getPaymentStatus());
        verify(orderService).releaseStock(order);
        verify(orderRepository).save(order);
    }

    @Test
    void paymentIntentCanceled_OrdineAnnullatoEStockRilasciato() {
        acceptSignature("payment_intent.canceled");
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.of(order));

        webhookService.handleEvent(bytes(payload("payment_intent.canceled")), "t=1,v1=x");

        assertEquals(PaymentStatus.CANCELED, order.getPaymentStatus());
        verify(orderService).releaseStock(order);
    }

    @Test
    void ordineGiaPagato_PagamentoRiuscitoNonLoTocca() {
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.of(order));
        order.setPaymentStatus(PaymentStatus.PAID);

        assertDoesNotThrow(
                () -> webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x"));

        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void ordineFallito_PagamentoRiuscitoLoPortaAPaid() {
        // I soldi sono stati presi: lo stato dell'ordine deve seguirli
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.of(order));
        order.setPaymentStatus(PaymentStatus.FAILED);

        webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x");

        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        verify(orderService, never()).releaseStock(any(Order.class));
    }

    @Test
    void eventoDiUnOggettoNonDiPagamento_VieneRegistratoEDeurIgnorato() {
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(false);

        assertDoesNotThrow(
                () -> webhookService.handleEvent(bytes(payload("evt_1", "charge.updated", "ch_1", "charge")),
                        "t=1,v1=x"));

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderService, never()).releaseStock(any(Order.class));
        verify(eventRepository).saveAndFlush(any(StripeWebhookEvent.class));
    }

    @Test
    void ordineSconosciuto_LEventoVieneAccettatoSenzaErrori() {
        acceptSignature();
        when(eventRepository.existsById("evt_1")).thenReturn(false);
        when(orderRepository.findByStripePaymentIntent("pi_1")).thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> webhookService.handleEvent(bytes(payload("payment_intent.succeeded")), "t=1,v1=x"));

        verify(orderRepository, never()).save(any(Order.class));
    }
}
