package fpt.tuanhm43.server.exceptions;

import org.springframework.http.HttpStatus;

public class ReservationExpiredException extends BusinessException {
    public ReservationExpiredException(String message) {
        super(message, HttpStatus.GONE);
    }
}
