package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}
