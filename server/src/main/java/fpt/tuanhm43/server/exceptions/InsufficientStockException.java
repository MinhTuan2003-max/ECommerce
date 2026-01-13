package fpt.tuanhm43.server.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InsufficientStockException(UUID variantId, Integer requested, Integer available) {
        super(String.format("Insufficient stock for variant '%s': requested %d, available %d",
                variantId, requested, available), HttpStatus.BAD_REQUEST);
    }
}
