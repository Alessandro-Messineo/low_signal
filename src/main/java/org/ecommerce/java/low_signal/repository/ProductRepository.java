package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
