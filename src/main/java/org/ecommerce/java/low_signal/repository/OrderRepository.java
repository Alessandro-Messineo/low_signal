package org.ecommerce.java.low_signal.repository;

import java.util.List;

import org.ecommerce.java.low_signal.model.Order;
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
}
