package fpt.tuanhm43.server.dtos.cart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cart Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private String sessionId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private String message;
    private Boolean hasStockIssues;

    /**
     * Empty cart response
     */
    public static CartResponse empty(String sessionId) {
        return CartResponse.builder()
                .sessionId(sessionId)
                .items(List.of())
                .totalItems(0)
                .totalAmount(BigDecimal.ZERO)
                .message("Your cart is empty")
                .hasStockIssues(false)
                .build();
    }
}
