package org.ecommerce.java.low_signal.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.ecommerce.java.low_signal.LowSignalApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Difesa 1 end-to-end: l'applicazione NON deve riuscire ad avviarsi con una
 * chiave Stripe live. Se questo test fallisce, un pagamento REALE sarebbe
 * possibile.
 */
class StripeGuardIntegrationTest {

    private static final String BLOCK_MARKER = "REALE BLOCCATO";

    @Test
    void applicazioneConChiaveLive_NonParte() {
        ConfigurableApplicationContext context = null;
        try {
            context = new SpringApplicationBuilder(LowSignalApplication.class)
                    .profiles("test")
                    .properties(
                            "server.port=0",
                            "spring.devtools.restart.enabled=false",
                            "spring.main.banner-mode=off")
                    // argomenti da command line: priorita' superiore ai file di profile
                    .run(
                            "--stripe.secret-key=sk_live_should_never_run_here",
                            // DB dedicato: chiudere questo contesto non deve toccare i dati dei test
                            "--spring.datasource.url=jdbc:h2:mem:stripe_guard_db;DB_CLOSE_DELAY=-1");
            fail("L'applicazione e' partita con una chiave LIVE: il pagamento reale sarebbe possibile!");
        } catch (Exception ex) {
            assertTrue(hasCauseMessage(ex, BLOCK_MARKER),
                    "Atteso il rifiuto del guard Stripe, ma l'errore e': " + describe(ex));
        } finally {
            if (context != null && context.isActive()) {
                context.close();
            }
        }
    }

    @Test
    void applicazioneConModalitaLive_NonParte() {
        ConfigurableApplicationContext context = null;
        try {
            context = new SpringApplicationBuilder(LowSignalApplication.class)
                    .profiles("test")
                    .properties(
                            "server.port=0",
                            "spring.devtools.restart.enabled=false",
                            "spring.main.banner-mode=off")
                    .run(
                            "--stripe.mode=live",
                            // DB dedicato: chiudere questo contesto non deve toccare i dati dei test
                            "--spring.datasource.url=jdbc:h2:mem:stripe_guard_db;DB_CLOSE_DELAY=-1");
            fail("L'applicazione e' partita con stripe.mode=live: il pagamento reale sarebbe possibile!");
        } catch (Exception ex) {
            assertTrue(hasCauseMessage(ex, BLOCK_MARKER),
                    "Atteso il rifiuto del guard Stripe, ma l'errore e': " + describe(ex));
        } finally {
            if (context != null && context.isActive()) {
                context.close();
            }
        }
    }

    private boolean hasCauseMessage(Throwable ex, String needle) {
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(needle)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String describe(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable current = ex;
        while (current != null) {
            sb.append(current.getClass().getSimpleName())
                    .append(": ")
                    .append(current.getMessage())
                    .append(" | ");
            current = current.getCause();
        }
        return sb.toString();
    }
}
