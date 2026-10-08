package org.ecommerce.java.low_signal.dto;

/**
 * Risposta di GET /payments/status/{orderId}: stato dell'ordine piu' lo stato
 * reale del PaymentIntent interrogato direttamente su Stripe.
 */
public class PaymentStatusResponse {

    private Integer orderId;
    private String orderNumber;
    private String paymentStatus;
    private String stripeStatus;
    private String paymentIntentId;

    public Integer getOrderId() {
        return this.orderId;
    }

    public void setOrderId(Integer value) {
        this.orderId = value;
    }

    public String getOrderNumber() {
        return this.orderNumber;
    }

    public void setOrderNumber(String value) {
        this.orderNumber = value;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String value) {
        this.paymentStatus = value;
    }

    public String getStripeStatus() {
        return this.stripeStatus;
    }

    public void setStripeStatus(String value) {
        this.stripeStatus = value;
    }

    public String getPaymentIntentId() {
        return this.paymentIntentId;
    }

    public void setPaymentIntentId(String value) {
        this.paymentIntentId = value;
    }
}
