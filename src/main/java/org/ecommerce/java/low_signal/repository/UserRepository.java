package org.ecommerce.java.low_signal.repository;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

     Optional<User> findByEmail(String email);

}
