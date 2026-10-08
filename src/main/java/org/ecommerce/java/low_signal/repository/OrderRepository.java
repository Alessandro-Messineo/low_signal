package org.ecommerce.java.low_signal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.ecommerce.java.low_signal.model.Order;
import org.ecommerce.java.low_signal.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shippingAddress " +
            "LEFT JOIN FETCH o.billingAddress " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "LEFT JOIN FETCH oi.productVariant " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetails();

    @Query("SELECT o FROM Order o " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH o.shippingAddress " +
            "LEFT JOIN FETCH o.billingAddress " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "LEFT JOIN FETCH oi.productVariant " +
            "WHERE u.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdWithDetails(Integer userId);

    /** Ordine il cui pagamento e' stato tracciato con questo PaymentIntent Stripe. */
    Optional<Order> findByStripePaymentIntent(String stripePaymentIntent);

    /** Ordini ancora in attesa di pagamento creati prima della soglia (scadenza). */
    List<Order> findByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime createdAt);
}
