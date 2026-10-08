package org.ecommerce.java.low_signal.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.ecommerce.java.low_signal.exception.PaymentException;
import org.ecommerce.java.low_signal.service.StripeGateway;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.stripe.StripeClient;
import com.stripe.model.Event;

/**
 * Verifica della firma del webhook con firma HMAC costruita A MANO:
 * e' esattamente il calcolo che Stripe fa prima di chiamare /payments/webhook.
 * Nessuna chiamata di rete: la verifica avviene interamente in locale.
 */
class StripeGatewayWebhookSignatureTest {

    private static final String SECRET = "whsec_test_dummy_signature";
    private static final String PAYLOAD = """
            {"id":"evt_test_1","type":"payment_intent.succeeded","data":{"object":{"id":"pi_1","object":"payment_intent"}}}""";

    private final StripeGateway gateway = new StripeGateway(new StripeClient("sk_test_dummy"));

    @Test
    void firmaValida_LoREventoVieneAccettato() {
        String header = sign(PAYLOAD, SECRET, Instant.now().getEpochSecond());

        Event event = gateway.verifyWebhook(PAYLOAD, header, SECRET);

        assertEquals("evt_test_1", event.getId());
        assertEquals("payment_intent.succeeded", event.getType());
    }

    @Test
    void payloadManomesso_FirmaNonValida() {
        String header = sign(PAYLOAD, SECRET, Instant.now().getEpochSecond());
        String tampered = PAYLOAD.replace("pi_1", "pi_HACKED");

        PaymentException ex = assertThrows(PaymentException.class,
                () -> gateway.verifyWebhook(tampered, header, SECRET));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("Firma"));
    }

    @Test
    void firmaCalcolataConUnAltroSegreto_VieneRifiutata() {
        String header = sign(PAYLOAD, "whsec_quello_sbagliato", Instant.now().getEpochSecond());

        PaymentException ex = assertThrows(PaymentException.class,
                () -> gateway.verifyWebhook(PAYLOAD, header, SECRET));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void headerAssente_VieneRifiutato() {
        PaymentException ex = assertThrows(PaymentException.class,
                () -> gateway.verifyWebhook(PAYLOAD, null, SECRET));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void segretoNonConfigurato_VieneRifiutato() {
        String header = sign(PAYLOAD, SECRET, Instant.now().getEpochSecond());

        PaymentException ex = assertThrows(PaymentException.class,
                () -> gateway.verifyWebhook(PAYLOAD, header, ""));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void firmaTroppoVecchia_VieneRifiutata() {
        long old = Instant.now().minusSeconds(3600).getEpochSecond();
        String header = sign(PAYLOAD, SECRET, old);

        assertThrows(PaymentException.class, () -> gateway.verifyWebhook(PAYLOAD, header, SECRET));
    }

    /** header Stripe-Signature: t=<timestamp>,v1=hmac_sha256(secret, "<t>.<payload>") */
    private static String sign(String payload, String secret, long timestamp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
            String v1 = HexFormat.of().formatHex(digest);
            return "t=" + timestamp + ",v1=" + v1;
        } catch (Exception e) {
            throw new IllegalStateException("Impossibile calcolare la firma di test", e);
        }
    }
}
