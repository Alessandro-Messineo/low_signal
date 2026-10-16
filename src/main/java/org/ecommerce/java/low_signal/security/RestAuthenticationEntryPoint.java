package org.ecommerce.java.low_signal.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Risponde 401 (con body JSON) quando una richiesta protetta arriva senza una
 * sessione JWT valida.
 *
 * Senza questo entry point Spring Security usa Http403ForbiddenEntryPoint e
 * risponde 403: il frontend, che rinnova il token solo sui 401, non puo'
 * distinguere "token scaduto" da "rifiuto CSRF" e non riprova mai il refresh.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter()
                .write("{\"error\":\"Non autenticato: sessione scaduta, rifai il login\"}");
    }
}
