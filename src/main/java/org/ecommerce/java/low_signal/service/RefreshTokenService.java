package org.ecommerce.java.low_signal.service;

import java.time.Instant;
import java.util.UUID;

import org.ecommerce.java.low_signal.model.RefreshToken;
import org.ecommerce.java.low_signal.model.User;
import org.ecommerce.java.low_signal.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration; // es. 604800000 = 7 giorni in ms

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Crea un nuovo refresh token per l'utente e lo salva nel DB
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Cancella eventuali token precedenti dello stesso utente (one-token-per-user)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    // Verifica che il token esista, non sia revocato e non sia scaduto
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    // Revoca tutti i token dell'utente (usato nel logout)
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
