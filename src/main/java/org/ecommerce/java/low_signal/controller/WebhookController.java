package org.ecommerce.java.low_signal.controller;

import java.util.Map;

import org.ecommerce.java.low_signal.service.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint pubblico raggiungibile solo da Stripe.
 *
 * - il corpo arriva come byte[] grezzi: la verifica della firma HMAC deve
 *   avvenire sugli esatti byte ricevuti;
 * - nessuna autenticazione JWT, nessun CSRF (Stripe non puo' mandare il
 *   token XSRF): la sicurezza e' interamente nella firma;
 * - duplicate: se l'eid dell'evento esiste gia', l'evento viene scartato.
 */
@RestController
@RequestMapping("/payments")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody byte[] payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String signature) {
        try {
            webhookService.handleEvent(payload, signature);
        } catch (DataIntegrityViolationException e) {
            // Evento identico processato in parallelo da un'altra richiesta
            log.info("Evento Stripe duplicato ricevuto in parallelo, ACK");
            return ResponseEntity.ok(Map.of("received", true, "duplicate", true));
        }
        return ResponseEntity.ok(Map.of("received", true));
    }
}
