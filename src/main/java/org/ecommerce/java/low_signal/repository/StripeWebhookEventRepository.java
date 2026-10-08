package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, String> {
}
