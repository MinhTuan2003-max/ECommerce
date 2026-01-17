package fpt.tuanhm43.server.dtos.cart.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

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

    private Integer availableStock;
    private Boolean inStock;
    private String stockMessage;

    private Boolean priceChanged;
    private BigDecimal currentPrice;

    public void updateStockMessage() {
        if (availableStock == null) {
            this.inStock = false;
            this.stockMessage = "Stock information unavailable";
        } else if (availableStock == 0) {
            this.inStock = false;
            this.stockMessage = "Out of stock";
        } else if (availableStock < quantity) {
            this.inStock = false;
            this.stockMessage = String.format("Only %d in stock (need %d)", availableStock, quantity);
        } else if (availableStock < 5) {
            this.inStock = true;
            this.stockMessage = String.format("Low stock: %d remaining", availableStock);
        } else {
            this.inStock = true;
            this.stockMessage = "In stock";
        }
    }
}