package fpt.tuanhm43.server.exceptions;

import fpt.tuanhm43.server.exceptions.base.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}