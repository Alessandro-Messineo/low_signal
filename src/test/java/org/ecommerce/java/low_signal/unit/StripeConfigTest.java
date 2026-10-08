package org.ecommerce.java.low_signal.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.ecommerce.java.low_signal.config.StripeConfig;
import org.junit.jupiter.api.Test;

/**
 * Le due difese anti-pagamento-reale: l'applicazione non parte con una chiave
 * live o con la modalita' sbagliata.
 */
class StripeConfigTest {

    @Test
    void chiaveLive_VieneBloccata() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> StripeConfig.assertTestOnly("sk_live_1234567890", "test"));

        assertTrue(ex.getMessage().contains("sk_live_"));
        assertTrue(ex.getMessage().contains("REALE BLOCCATO"));
    }

    @Test
    void chiaveMancante_VieneBloccata() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> StripeConfig.assertTestOnly(null, "test"));

        assertTrue(ex.getMessage().contains("REALE BLOCCATO"));
    }

    @Test
    void chiaveVuota_VieneBloccata() {
        assertThrows(IllegalStateException.class,
                () -> StripeConfig.assertTestOnly("   ", "test"));
    }

    @Test
    void modalitaNonTest_VieneBloccata() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> StripeConfig.assertTestOnly("sk_test_ok", "live"));

        assertTrue(ex.getMessage().contains("stripe.mode"));
    }

    @Test
    void chiaveTest_VieneAccettata() {
        assertDoesNotThrow(() -> StripeConfig.assertTestOnly("sk_test_abcd1234", "test"));
    }

    @Test
    void preview_NonMaiLaChiaveCompleta() {
        String key = "sk_test_supersegreto1234567890";

        String preview = StripeConfig.preview(key);

        assertFalse(preview.contains("supersegreto"));
        assertTrue(preview.startsWith("sk_test_"));
    }
}
