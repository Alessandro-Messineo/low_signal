package org.ecommerce.java.low_signal.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Registro degli eventi Stripe ricevuti dal webhook. L'id dell'evento e' la
 * chiave primaria: serve sia da audit sia da idempotenza (Stripe puo'
 * rinviare lo stesso evento piu' volte).
 */
@Entity
@Table(name = "stripe_webhook_events")
public class StripeWebhookEvent {

    @Id
    @Column(name = "event_id", nullable = false, unique = true, length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payment_intent_id", length = 100)
    private String paymentIntentId;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    public StripeWebhookEvent() {
    }

    public String getEventId() {
        return this.eventId;
    }

    public void setEventId(String value) {
        this.eventId = value;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String value) {
        this.eventType = value;
    }

    public String getPaymentIntentId() {
        return this.paymentIntentId;
    }

    public void setPaymentIntentId(String value) {
        this.paymentIntentId = value;
    }

    public LocalDateTime getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(LocalDateTime value) {
        this.receivedAt = value;
    }
}
