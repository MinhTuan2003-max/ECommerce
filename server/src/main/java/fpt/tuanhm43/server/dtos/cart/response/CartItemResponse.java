package fpt.tuanhm43.server.dtos.cart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cart Item Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {

    private UUID cartItemId;
    private UUID variantId;
    private String sku;
    private String productName;
    private String size;
    private String color;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // Stock availability
    private Integer availableStock;
    private Boolean inStock;
    private String stockMessage;

    // Price change indicator
    private Boolean priceChanged;
    private BigDecimal currentPrice;
}