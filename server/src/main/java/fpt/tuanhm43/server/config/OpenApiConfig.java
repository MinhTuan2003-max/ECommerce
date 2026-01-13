package fpt.tuanhm43.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "E-Commerce Backend API",
                version = "1.0.0",
                description = """
                        RESTful API for E-Commerce Application
                        """,
                contact = @Contact(
                        name = "TuanHM43",
                        email = "tuanhm43@fpt.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = """
                Enter JWT token for admin/staff authentication.
                Note: Guest endpoints (products, cart, checkout) don't require authentication.
                """
)
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer globalResponseCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses apiResponses = operation.getResponses();

                    apiResponses.addApiResponse("400", createApiResponse(
                            "Bad Request",
                            "Invalid request parameters",
                            "BAD_REQUEST"
                    ));

                    apiResponses.addApiResponse("401", createApiResponse(
                            "Unauthorized",
                            "Please login to access this resource",
                            "UNAUTHORIZED"
                    ));

                    apiResponses.addApiResponse("403", createApiResponse(
                            "Forbidden",
                            "You don't have permission to access this resource",
                            "FORBIDDEN"
                    ));

                    apiResponses.addApiResponse("404", createApiResponse(
                            "Not Found",
                            "Resource not found",
                            "RESOURCE_NOT_FOUND"
                    ));

                    apiResponses.addApiResponse("409", createInventoryConflictResponse());

                    apiResponses.addApiResponse("422", createValidationErrorResponse());

                    apiResponses.addApiResponse("500", createApiResponse(
                            "Internal Server Error",
                            "An unexpected error occurred. Please contact support.",
                            "INTERNAL_SERVER_ERROR"
                    ));
                })
        );
    }

    /**
     * Generic API error response
     */
    private ApiResponse createApiResponse(
            String description,
            String message,
            String errorCode) {

        String example = String.format("""
                {
                  "success": false,
                  "message": "%s",
                  "data": null,
                  "error": {
                    "code": "%s",
                    "debugMessage": "Additional error details here"
                  },
                  "timestamp": "%s"
                }
                """, message, errorCode, LocalDateTime.now());

        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().example(example)
                        )
                );
    }

    private ApiResponse createValidationErrorResponse() {
        String example = String.format("""
                {
                  "success": false,
                  "message": "Validation failed",
                  "data": {
                    "customerEmail": "Invalid email format",
                    "customerPhone": "Phone number must be in format: +84xxxxxxxxx or 0xxxxxxxxx",
                    "shippingAddress": "Shipping address is required"
                  },
                  "error": {
                    "code": "VALIDATION_ERROR",
                    "debugMessage": "Field validation failed"
                  },
                  "timestamp": "%s"
                }
                """, LocalDateTime.now());

        return new ApiResponse()
                .description("Unprocessable Entity - Validation Failed")
                .content(new Content()
                        .addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().example(example)
                        )
                );
    }

    private ApiResponse createInventoryConflictResponse() {
        String example = String.format("""
                {
                  "success": false,
                  "message": "Insufficient stock for variant 102. Requested: 3, Available: 1",
                  "data": {
                    "variantId": 102,
                    "requested": 3,
                    "available": 1
                  },
                  "error": {
                    "code": "INSUFFICIENT_STOCK",
                    "debugMessage": "Not enough inventory"
                  },
                  "timestamp": "%s"
                }
                """, LocalDateTime.now());

        return new ApiResponse()
                .description("Conflict - Business Logic Error (e.g., out of stock, reservation expired)")
                .content(new Content()
                        .addMediaType(
                                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                                new MediaType().example(example)
                        )
                );
    }
}