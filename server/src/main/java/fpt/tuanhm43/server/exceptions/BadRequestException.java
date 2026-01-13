package fpt.tuanhm43.server.exceptions;

import fpt.tuanhm43.server.exceptions.base.BusinessException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BusinessException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}