package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

}
