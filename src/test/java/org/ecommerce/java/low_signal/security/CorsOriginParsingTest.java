package org.ecommerce.java.low_signal.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Normalizzazione di FRONTEND_URL per il CORS.
 * Un valore con slash finale o spazi non combacia mai con l'origine
 * del browser e fa finire ogni richiesta in 403: da qui la pulizia.
 */
class CorsOriginParsingTest {

    @Test
    void valorePulitoVieneAccettato() {
        assertEquals(List.of("https://lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins("https://lowsignal.vercel.app"));
    }

    @Test
    void slashFinaleERimuovo() {
        assertEquals(List.of("https://lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins("https://lowsignal.vercel.app/"));
    }

    @Test
    void spaziAiBordiVengonoTolte() {
        assertEquals(List.of("https://lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins("  https://lowsignal.vercel.app  "));
    }

    @Test
    void virgoletteAiBordiVengonoTolte() {
        assertEquals(List.of("https://lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins("\"https://lowsignal.vercel.app\""));
    }

    @Test
    void originiMultipleSeparateDaVirgola() {
        assertEquals(
                List.of("https://lowsignal.vercel.app", "https://www.lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins(
                        "https://lowsignal.vercel.app, https://www.lowsignal.vercel.app/"));
    }

    @Test
    void placeholderNonRisoltoVieneIgnorato() {
        assertTrue(SecurityConfiguration.parseExtraOrigins("${FRONTEND_URL}").isEmpty());
    }

    @Test
    void valoreMancanteONullo() {
        assertTrue(SecurityConfiguration.parseExtraOrigins(null).isEmpty());
        assertTrue(SecurityConfiguration.parseExtraOrigins("").isEmpty());
        assertTrue(SecurityConfiguration.parseExtraOrigins("   ").isEmpty());
    }

    @Test
    void slashMultipliVengonoTuttiTolti() {
        assertEquals(List.of("https://lowsignal.vercel.app"),
                SecurityConfiguration.parseExtraOrigins("https://lowsignal.vercel.app///"));
    }
}
