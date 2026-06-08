package org.ecommerce.java.low_signal.repository;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Integer> {

    Optional<Newsletter> findByEmail(String email);

    boolean existsByEmail(String email);

}
