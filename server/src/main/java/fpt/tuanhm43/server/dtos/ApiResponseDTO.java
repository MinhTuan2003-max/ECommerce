package fpt.tuanhm43.server.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponseDTO<T> {

    @Builder.Default
    @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "HTTP status code", example = "200")
    private int status;

    @Schema(description = "Response message", example = "Operation successful")
    private String message;

    @Schema(description = "Response data payload")
    private T data;

    @Schema(description = "Error details (only present when status >= 400)")
    private Object errors;

    /**
     * Creates a successful response with data.
     */
    public static <T> ApiResponseDTO<T> success(T data) {
        return ApiResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with data and message.
     */
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error response.
     */
    public static <T> ApiResponseDTO<T> error(int status, String message, Object errors) {
        return ApiResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .errors(errors)
                .build();
    }

    /**
     * Creates a created (201) response.
     */
    public static <T> ApiResponseDTO<T> created(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(201)
                .message(message)
                .data(data)
                .build();
    }
}