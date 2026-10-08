package org.ecommerce.java.low_signal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Attivazione della scheduling di Spring.
 *
 * Senza @EnableScheduling i metodi annotati con @Scheduled vengono ignorati
 * in silenzio (nessun errore, nessuna esecuzione): qui viene abilitata la
 * scadenza degli ordini non pagati gestita da PaymentExpiryScheduler.
 *
 * Tenuta in config/ invece che sull'entry point per non intasare
 * LowSignalApplication con annotazioni di funzionalita'.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
