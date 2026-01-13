package fpt.tuanhm43.server.exceptions;

import fpt.tuanhm43.server.exceptions.base.BusinessException;
import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends BusinessException {
    public PaymentProcessingException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}