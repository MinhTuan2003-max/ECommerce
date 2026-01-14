package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "Endpoints for managing the shopping cart session, adding items, and validating stock before checkout.")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart", description = "Retrieve all items in the current shopping cart associated with the user's session.")
    public ResponseEntity<ApiResponseDTO<CartResponse>> getCart(@Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Fetching cart for session: {}", sessionId);
        CartResponse cart = cartService.getBySessionId(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(cart));
    }

    @GetMapping("/count")
    @Operation(summary = "Get cart item count", description = "Returns the total number of unique items currently in the cart.")
    public ResponseEntity<ApiResponseDTO<Integer>> getItemCount(@Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Fetching cart item count for session: {}", sessionId);
        int count = cartService.getCartItemCount(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(count));
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart", description = "Add a product variant to the cart. If the variant already exists, the quantity will be incremented.")
    @ApiResponse(responseCode = "201", description = "Item added successfully")
    public ResponseEntity<ApiResponseDTO<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Adding item to cart - Session: {}, VariantId: {}, Quantity: {}",
                sessionId, request.getVariantId(), request.getQuantity());
        CartResponse cart = cartService.addToCart(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(cart, "Item added to cart successfully"));
    }

    @PutMapping("/items/{variantId}")
    @Operation(summary = "Update item quantity", description = "Update the quantity for a specific variant in the cart. Set quantity to 0 to remove the item.")
    public ResponseEntity<ApiResponseDTO<CartResponse>> updateCartItem(
            @Parameter(description = "UUID of the product variant") @PathVariable("variantId") UUID variantId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Updating cart item - Session: {}, VariantId: {}, NewQuantity: {}",
                sessionId, variantId, request.getQuantity());
        CartResponse cart = cartService.update(sessionId, variantId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/items/{variantId}")
    @Operation(summary = "Remove item from cart", description = "Completely remove a specific product variant from the current shopping cart.")
    public ResponseEntity<ApiResponseDTO<CartResponse>> removeFromCart(
            @Parameter(description = "UUID of the variant to remove") @PathVariable("variantId") UUID variantId,
            @Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Removing item from cart - Session: {}, VariantId: {}", sessionId, variantId);
        CartResponse cart = cartService.remove(sessionId, variantId);
        return ResponseEntity.ok(ApiResponseDTO.success(cart, "Item removed from cart successfully"));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the shopping cart for the current session.")
    public ResponseEntity<ApiResponseDTO<Void>> clearCart(@Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Clearing cart - Session: {}", sessionId);
        cartService.clear(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Cart cleared successfully"));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate cart stock", description = "Perform a real-time stock check for all items in the cart before proceeding to checkout.")
    public ResponseEntity<ApiResponseDTO<Boolean>> validateStock(@Parameter(hidden = true) HttpSession session) {
        String sessionId = session.getId();
        log.info("Validating cart stock - Session: {}", sessionId);
        boolean isValid = cartService.validateCartStock(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(isValid,
                isValid ? "All items are in stock" : "Some items are out of stock"));
    }
}