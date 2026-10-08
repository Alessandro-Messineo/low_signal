package org.ecommerce.java.low_signal.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Richiesta di creazione (o riuso) del PaymentIntent per un ordine.
 * L'importo NON viene mai mandato dal client: viene sempre letto dal database.
 */
public class CreatePaymentIntentRequest {

    @NotNull(message = "Order id is required")
    private Integer orderId;

    public CreatePaymentIntentRequest() {
    }

    public Integer getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Integer value) {
        this.orderId = value;
    }
}
