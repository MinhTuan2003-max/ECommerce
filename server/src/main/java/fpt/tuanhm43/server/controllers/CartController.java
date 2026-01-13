package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Cart Controller
 * Handles shopping cart operations (session-based)
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * Get current shopping cart
     * Uses session ID to track cart
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<CartResponse>> getCart(HttpSession session) {
        String sessionId = session.getId();
        log.info("Fetching cart for session: {}", sessionId);
        CartResponse cart = cartService.getCart(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(cart));
    }

    /**
     * Get total number of items in cart
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponseDTO<Integer>> getItemCount(HttpSession session) {
        String sessionId = session.getId();
        log.info("Fetching cart item count for session: {}", sessionId);
        int count = cartService.getCartItemCount(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(count));
    }

    /**
     * Add item to cart
     * If item already exists, quantity is accumulated
     */
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponseDTO<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            HttpSession session) {
        String sessionId = session.getId();
        log.info("Adding item to cart - Session: {}, VariantId: {}, Quantity: {}",
                sessionId, request.getVariantId(), request.getQuantity());
        CartResponse cart = cartService.addToCart(sessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.created(cart, "Item added to cart successfully"));
    }

    /**
     * Update cart item quantity
     * Pass quantity 0 to remove item
     */
    @PutMapping("/items/{variantId}")
    public ResponseEntity<ApiResponseDTO<CartResponse>> updateCartItem(
            @PathVariable("variantId") UUID variantId,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpSession session) {
        String sessionId = session.getId();
        log.info("Updating cart item - Session: {}, VariantId: {}, NewQuantity: {}",
                sessionId, variantId, request.getQuantity());
        CartResponse cart = cartService.updateCartItem(sessionId, variantId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(cart, "Cart item updated successfully"));
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<ApiResponseDTO<CartResponse>> removeFromCart(
            @PathVariable("variantId") UUID variantId,
            HttpSession session) {
        String sessionId = session.getId();
        log.info("Removing item from cart - Session: {}, VariantId: {}", sessionId, variantId);
        CartResponse cart = cartService.removeFromCart(sessionId, variantId);
        return ResponseEntity.ok(ApiResponseDTO.success(cart, "Item removed from cart successfully"));
    }

    /**
     * Clear all items from cart
     */
    @DeleteMapping
    public ResponseEntity<ApiResponseDTO<Void>> clearCart(HttpSession session) {
        String sessionId = session.getId();
        log.info("Clearing cart - Session: {}", sessionId);
        cartService.clearCart(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Cart cleared successfully"));
    }

    /**
     * Validate stock availability for all items in cart
     * Returns true if all items are available
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponseDTO<Boolean>> validateStock(HttpSession session) {
        String sessionId = session.getId();
        log.info("Validating cart stock - Session: {}", sessionId);
        boolean isValid = cartService.validateCartStock(sessionId);
        return ResponseEntity.ok(ApiResponseDTO.success(isValid,
                isValid ? "All items are in stock" : "Some items are out of stock"));
    }
}
