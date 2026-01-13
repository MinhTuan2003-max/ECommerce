package fpt.tuanhm43.server.exceptions.base;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final HttpStatus status;

    protected BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}