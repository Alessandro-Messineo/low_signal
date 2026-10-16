package org.ecommerce.java.low_signal.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rifiuto 403 con body JSON.
 *
 * Mantiene lo 403 (il problema non e' l'autenticazione) ma rende il motivo
 * esplicito: senza il body la risposta e' vuota e un 403 non si puo' capire.
 *
 * L'handler e' globale (lo usano sia CsrfFilter che ExceptionTranslationFilter),
 * quindi distingue il rifiuto CSRF da un rifiuto di autorizzazione: senza questo
 * controllo un 403 normale verrebbe dichiarato "CSRF token missing or invalid".
 */
public class JsonCsrfAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(isCsrfFailure(accessDeniedException)
                ? "{\"error\":\"CSRF token missing or invalid\"}"
                : "{\"error\":\"Access denied\"}");
    }

    private boolean isCsrfFailure(AccessDeniedException exception) {
        return exception instanceof MissingCsrfTokenException
                || exception instanceof InvalidCsrfTokenException;
    }
}
