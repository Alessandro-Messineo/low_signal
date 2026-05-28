package org.ecommerce.java.low_signal.repository;

import java.util.Optional;

import org.ecommerce.java.low_signal.model.RefreshToken;
import org.ecommerce.java.low_signal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Cancella tutti i refresh token di un utente (usato al logout o al rinnovo)
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.user = :user")
    void deleteByUser(User user);
}
