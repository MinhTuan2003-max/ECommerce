package fpt.tuanhm43.server.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidStateTransitionException extends BusinessException {
    public InvalidStateTransitionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
