package org.ecommerce.java.low_signal.repository;

import org.ecommerce.java.low_signal.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Integer> {
    
}