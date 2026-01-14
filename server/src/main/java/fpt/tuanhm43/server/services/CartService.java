package fpt.tuanhm43.server.services;

import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;

import java.util.UUID;

public interface CartService {

    /**
     * Get shopping cart by session ID
     *
     * @param sessionId Session identifier
     * @return CartResponse
     */
    CartResponse getBySessionId(String sessionId);

    /**
     * Add item to cart
     *
     * @param sessionId Session identifier
     * @param request Add to cart request
     * @return Updated cart
     */
    CartResponse addToCart(String sessionId, AddToCartRequest request);

    /**
     * Update cart item quantity
     *
     * @param sessionId Session identifier
     * @param variantId Product variant ID
     * @param request Update request
     * @return Updated cart
     */
    CartResponse update(String sessionId, UUID variantId, UpdateCartItemRequest request);

    /**
     * Remove item from cart
     *
     * @param sessionId Session identifier
     * @param variantId Product variant ID
     * @return Updated cart
     */
    CartResponse remove(String sessionId, UUID variantId);

    /**
     * Clear all items from cart
     *
     * @param sessionId Session identifier
     */
    void clear(String sessionId);

    /**
     * Validate stock availability for all items in cart
     *
     * @param sessionId Session identifier
     * @return true if all items are available
     */
    boolean validateCartStock(String sessionId);

    /**
     * Get total number of items in cart
     *
     * @param sessionId Session identifier
     * @return Item count
     */
    int getCartItemCount(String sessionId);
}