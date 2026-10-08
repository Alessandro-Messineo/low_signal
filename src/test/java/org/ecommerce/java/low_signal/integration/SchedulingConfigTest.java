package org.ecommerce.java.low_signal.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.ecommerce.java.low_signal.service.PaymentExpiryScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.test.context.ActiveProfiles;

/**
 * Senza @EnableScheduling (in config/SchedulingConfig) i metodi @Scheduled
 * vengono ignorati in silenzio: nessun errore, nessuna esecuzione, stock che
 * non torna mai disponibile. Questo test rende l'errore visibile.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchedulingConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void schedulingAttivata_NessunScheduledIgnorato() {
        assertNotNull(context.getBean(ScheduledAnnotationBeanPostProcessor.class),
                "Scheduling non attiva: @EnableScheduling mancante su SchedulingConfig, "
                        + "i metodi @Scheduled non verrebbero mai eseguiti");
    }

    @Test
    void schedulerDellaScadenzaOrdini_Registrato() {
        assertNotNull(context.getBean(PaymentExpiryScheduler.class),
                "PaymentExpiryScheduler non registrato: la scadenza degli ordini non partirebbe");
    }
}
