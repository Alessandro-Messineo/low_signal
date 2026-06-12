package org.ecommerce.java.low_signal.repository;

import java.util.List;
import java.util.Optional;

import org.ecommerce.java.low_signal.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findBySlug(String slug);
    
     List<Product> findByCollectionIdAndSlugNot(Integer collectionId, String slug);
}
