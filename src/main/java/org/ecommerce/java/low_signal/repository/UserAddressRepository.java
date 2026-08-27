package org.ecommerce.java.low_signal.repository;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {

    Optional<UserAddress> findByIdAndUserId(Integer id, Integer userId);
}
