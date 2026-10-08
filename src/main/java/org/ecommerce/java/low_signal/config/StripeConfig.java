package org.ecommerce.java.low_signal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stripe.StripeClient;

/**
 * Configurazione Stripe.
 *
 * DIFESA 1: la applicazione si rifiuta di partire se la chiave Stripe non e'
 * una chiava di TEST (sk_test_). Un pagamento REALE e' quindi impossibile.
 *
 * DIFESA 2: la modalita' deve essere esplicitamente "test" (stripe.mode) e
 * all'avvio viene stampato in console un banner ben visibile.
 */
@Configuration
public class StripeConfig {

    private static final Logger log = LoggerFactory.getLogger(StripeConfig.class);

    /** Prefisso delle uniche chiavi ammesse in questo progetto. */
    public static final String TEST_KEY_PREFIX = "sk_test_";

    /** Modalita' unica accettata. */
    public static final String ALLOWED_MODE = "test";

    @Value("${stripe.secret-key:${STRIPE_SECRET_KEY:}}")
    private String secretKey;

    // Fallback su STRIPE_MODE di .env se la property non e' presente
    @Value("${stripe.mode:${STRIPE_MODE:test}}")
    private String mode;

    @Bean
    StripeClient stripeClient() {
        assertTestOnly(secretKey, mode);
        printBanner(secretKey);
        return new StripeClient(secretKey);
    }

    /**
     * Validazione delle chiavi Stripe (visibile per i test).
     *
     * @throws IllegalStateException se la chiave manca, non e' di test, o la
     *                               modalita' non e' "test"
     */
    public static void assertTestOnly(String key, String mode) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "STRIPE_SECRET_KEY mancante o vuota. Progetto didattico: aggiungi una chiave di TEST "
                            + "(sk_test_...) nel file .env. Pagamento REALE BLOCCATO.");
        }
        if (!key.startsWith(TEST_KEY_PREFIX)) {
            throw new IllegalStateException(
                    "STRIPE_SECRET_KEY NON e' una chiava di TEST (" + preview(key) + "). "
                            + "Progetto didattico: usa SOLO chiavi sk_test_ prese dal dashboard Stripe "
                            + "(modalita' test). Pagamento REALE BLOCCATO.");
        }
        if (!ALLOWED_MODE.equals(mode)) {
            throw new IllegalStateException(
                    "stripe.mode deve essere '" + ALLOWED_MODE + "' in questo progetto, trovato: " + mode
                            + ". Pagamento REALE BLOCCATO.");
        }
    }

    /** Restituisce i primi caratteri della chiave, mai la chiave completa. */
    public static String preview(String key) {
        if (key == null || key.isEmpty()) {
            return "(vuota)";
        }
        int len = Math.min(key.length(), 8);
        return key.substring(0, len) + "...";
    }

    private void printBanner(String key) {
        log.warn("**************************************************************");
        log.warn("*** STRIPE IN MODALITA' TEST - NESSUN PAGAMENTO REALE    ***");
        log.warn("*** chiave: {} | stripe.mode={}                ***", preview(key), mode);
        log.warn("**************************************************************");
    }
}
