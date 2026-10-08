package org.ecommerce.java.low_signal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Rilascio periodico dello stock degli ordini non pagati.
 *
 * Senza questo task un ordine creato e mai pagato (utente che chiude la
 * finestra) bloccherebbe il magazzino per sempre: l'ordine viene annullato e
 * lo stock torna disponibile dopo app.payment.expiry-minutes.
 *
 * Il primo avvio e' ritardato (initial delay) per non interferire con i test.
 */
@Component
public class PaymentExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentExpiryScheduler.class);

    private final PaymentService paymentService;
    private final boolean enabled;

    public PaymentExpiryScheduler(PaymentService paymentService,
            @Value("${app.payment.expiry-enabled:${PAYMENT_EXPIRY_ENABLED:true}}") boolean enabled) {
        this.paymentService = paymentService;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${app.payment.expiry-delay-ms:300000}",
            initialDelayString = "${app.payment.expiry-initial-delay-ms:300000}")
    public void releaseExpiredOrders() {
        if (!enabled) {
            return;
        }
        try {
            int released = paymentService.releaseExpiredOrders();
            if (released > 0) {
                log.info("Scadenza ordini: {} ordini annullati e stock rilasciato", released);
            }
        } catch (Exception e) {
            log.error("Errore durante il rilascio degli ordini scaduti", e);
        }
    }
}
