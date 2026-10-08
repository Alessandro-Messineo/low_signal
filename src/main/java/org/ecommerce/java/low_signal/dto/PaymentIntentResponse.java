package org.ecommerce.java.low_signal.dto;

/**
 * Risposta di POST /payments/intent: contiene il client_secret che il front
 * usa per montare il Payment Element e confermare il pagamento.
 */
public class PaymentIntentResponse {

    private Integer orderId;
    private String orderNumber;
    private String paymentIntentId;
    private String clientSecret;
    private String stripeStatus;
    private String paymentStatus;
    private Long amountCents;
    private String currency;

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

    public String getPaymentIntentId() {
        return this.paymentIntentId;
    }

    public void setPaymentIntentId(String value) {
        this.paymentIntentId = value;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String value) {
        this.clientSecret = value;
    }

    public String getStripeStatus() {
        return this.stripeStatus;
    }

    public void setStripeStatus(String value) {
        this.stripeStatus = value;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String value) {
        this.paymentStatus = value;
    }

    public Long getAmountCents() {
        return this.amountCents;
    }

    public void setAmountCents(Long value) {
        this.amountCents = value;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String value) {
        this.currency = value;
    }
}
