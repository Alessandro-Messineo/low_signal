package org.ecommerce.java.low_signal.controller;

import java.util.Map;

import org.ecommerce.java.low_signal.dto.CreatePaymentIntentRequest;
import org.ecommerce.java.low_signal.dto.PaymentIntentResponse;
import org.ecommerce.java.low_signal.dto.PaymentStatusResponse;
import org.ecommerce.java.low_signal.security.DatabaseUserDetails;
import org.ecommerce.java.low_signal.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Crea (o riusa) il PaymentIntent dell'ordine e restituisce il
     * client_secret da passare al Payment Element.
     * L'importo viene calcolato dal backend: il client manda solo l'orderId.
     */
    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        PaymentIntentResponse response = paymentService.createIntent(currentUserId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Stato reale del pagamento: ordine + PaymentIntent interrogato su Stripe.
     * Da chiamare al ritorno dalla pagina di pagamento (il webhook arriva dopo).
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable Integer orderId) {
        return ResponseEntity.ok(paymentService.getStatus(currentUserId(), orderId));
    }

    /**
     * Annulla gli ordini PENDING scaduti e ne rilascia lo stock.
     * Richiede il ruolo ADMIN (vedi SecurityConfiguration).
     */
    @PostMapping("/release-expired")
    public ResponseEntity<Map<String, Integer>> releaseExpired() {
        return ResponseEntity.ok(Map.of("released", paymentService.releaseExpiredOrders()));
    }

    private Integer currentUserId() {
        DatabaseUserDetails userDetails = (DatabaseUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }
}
