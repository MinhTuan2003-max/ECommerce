package fpt.tuanhm43.server.exceptions;

import fpt.tuanhm43.server.exceptions.base.BusinessException;
import org.springframework.http.HttpStatus;

public class ReservationExpiredException extends BusinessException {
    public ReservationExpiredException(String message) {
        super(message, HttpStatus.GONE);
    }
}
