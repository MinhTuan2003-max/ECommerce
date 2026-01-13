package fpt.tuanhm43.server.exceptions;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(HttpStatus.NOT_FOUND.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        org.springframework.validation.FieldError::getField,
                        fe -> Objects.toString(fe.getDefaultMessage(), "")
                ));

        HttpStatus status = HttpStatus.resolve(422);
        return ResponseEntity.status(status != null ? status : HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(422, "Validation failed", errors));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.error(HttpStatus.FORBIDDEN.value(), "Access Denied", ex.getMessage()));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleConflict(OptimisticLockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception caught in GlobalExceptionHandler", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", ex.getMessage()));
    }
}
