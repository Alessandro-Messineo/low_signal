package org.ecommerce.java.low_signal.service;

import java.util.Map;

import org.ecommerce.java.low_signal.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;

/**
 * Unica porta d'accesso a Stripe: isola il resto dell'applicazione dai tipi
 * del SDK e traduce gli errori di Stripe in {@link PaymentException}.
 *
 * Le eccezioni sono catturabili e verificabili con Mockito, quindi i servizi
 * che la usano si testano senza rete.
 */
@Component
public class StripeGateway {

    private static final Logger log = LoggerFactory.getLogger(StripeGateway.class);

    /** Minimal view di un PaymentIntent usata dal resto dell'applicazione. */
    public record PaymentIntentData(String id, String status, String clientSecret, Long amount) {
    }

    private final StripeClient stripeClient;

    public StripeGateway(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    /**
     * Crea un PaymentIntent in modalita' test.
     *
     * @param amountCents     importo in centesimi, SEMPRE derivato dal database
     * @param currency        valuta ISO 4217 minuscola (es. "eur")
     * @param metadata        metadati salvati sull'intent (orderId, orderNumber...)
     * @param idempotencyKey  chiave di idempotenza Stripe: la stessa chiave
     *                        restituisce lo stesso intent invece di crearne uno nuovo
     */
    public PaymentIntentData createPaymentIntent(long amountCents, String currency,
            Map<String, String> metadata, String idempotencyKey) {
        PaymentIntentCreateParams.Builder params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency)
                // La conferma e' lato server e il design prevede solo la carta:
                // esplicitiamo il tipo invece di usare automatic_payment_methods,
                // che abiliterebbe metodi non attivati sull'account (Klarna,
                // Satispay, Amazon Pay...) e non permette di restringerli dal client.
                .addPaymentMethodType("card");

        if (metadata != null) {
            metadata.forEach(params::putMetadata);
        }

        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey(idempotencyKey)
                .build();

        try {
            PaymentIntent intent = stripeClient.v1().paymentIntents().create(params.build(), options);
            return toData(intent);
        } catch (StripeException e) {
            throw stripeError("Impossibile creare il pagamento su Stripe", e);
        }
    }

    /** Recupera uno stato reale del PaymentIntent da Stripe. */
    public PaymentIntentData retrievePaymentIntent(String paymentIntentId) {
        try {
            return toData(stripeClient.v1().paymentIntents().retrieve(paymentIntentId));
        } catch (StripeException e) {
            throw stripeError("Impossibile leggere il pagamento da Stripe", e);
        }
    }

    /** Annulla un PaymentIntent: dopo l'annullamento il client_secret non e' piu' pagabile. */
    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            stripeClient.v1().paymentIntents().cancel(paymentIntentId);
        } catch (StripeException e) {
            throw stripeError("Impossibile annullare il pagamento su Stripe", e);
        }
    }

    /**
     * Verifica la firma HMAC dell'header Stripe-Signature sul corpo esatto della
     * richiesta. Senza firma valida il webhook viene rigettato con 401.
     *
     * @throws PaymentException 401 se la firma non e' valida o manca
     */
    public Event verifyWebhook(String payload, String signatureHeader, String webhookSecret) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw PaymentException.unauthorized("Header Stripe-Signature mancante");
        }
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw PaymentException.notConfigured(
                    "STRIPE_WEBHOOK_SECRET non configurato: impossibile verificare il webhook");
        }
        try {
            return stripeClient.constructEvent(payload, signatureHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Webhook rifiutato, firma non valida: {}", e.getMessage());
            throw PaymentException.unauthorized("Firma del webhook non valida");
        }
    }

    private PaymentIntentData toData(PaymentIntent intent) {
        return new PaymentIntentData(intent.getId(), intent.getStatus(), intent.getClientSecret(),
                intent.getAmount());
    }

    private PaymentException stripeError(String message, StripeException e) {
        log.warn("Errore Stripe: {}", e.getMessage());
        return new PaymentException(HttpStatus.BAD_GATEWAY, message + ": " + e.getMessage());
    }
}
