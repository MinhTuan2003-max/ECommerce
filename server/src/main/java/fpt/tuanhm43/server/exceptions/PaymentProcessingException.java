package fpt.tuanhm43.server.exceptions;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends BusinessException {
    public PaymentProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}