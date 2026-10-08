package org.ecommerce.java.low_signal.exception;

import org.springframework.http.HttpStatus;

/**
 * Eccezione del dominio pagamenti. Viene tradotta in una risposta JSON pulita
 * dal {@link GlobalExceptionHandler} invece di generare un 500 con stacktrace.
 */
public class PaymentException extends RuntimeException {

    private final HttpStatus status;

    public PaymentException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static PaymentException badRequest(String message) {
        return new PaymentException(HttpStatus.BAD_REQUEST, message);
    }

    public static PaymentException unauthorized(String message) {
        return new PaymentException(HttpStatus.UNAUTHORIZED, message);
    }

    public static PaymentException forbidden(String message) {
        return new PaymentException(HttpStatus.FORBIDDEN, message);
    }

    public static PaymentException notFound(String message) {
        return new PaymentException(HttpStatus.NOT_FOUND, message);
    }

    public static PaymentException conflict(String message) {
        return new PaymentException(HttpStatus.CONFLICT, message);
    }

    public static PaymentException stripeUnavailable(String message) {
        return new PaymentException(HttpStatus.BAD_GATEWAY, message);
    }

    public static PaymentException notConfigured(String message) {
        return new PaymentException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
