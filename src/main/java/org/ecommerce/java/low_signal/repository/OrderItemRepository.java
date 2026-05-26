package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

}
