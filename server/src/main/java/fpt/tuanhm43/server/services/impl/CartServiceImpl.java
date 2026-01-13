package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.constants.AppConstants;
import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartItemResponse;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.entities.ShoppingCart;
import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.CartItemRepository;
import fpt.tuanhm43.server.repositories.ShoppingCartRepository;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public CartResponse getCart(String sessionId) {
        log.debug("Getting cart for session: {}", sessionId);

        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElse(null);

        if (cart == null || !cart.hasItems()) {
            return CartResponse.empty(sessionId);
        }

        // Extend expiration on each access
        cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(String sessionId, AddToCartRequest request) {
        log.info("Adding item to cart - Session: {}, VariantId: {}, Quantity: {}",
                sessionId, request.getVariantId(), request.getQuantity());

        // 1. Get or create cart
        ShoppingCart cart = getOrCreateCart(sessionId);

        // 2. Get product variant
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ProductVariant", "id", request.getVariantId()));

        // 3. Get inventory
        Inventory inventory = inventoryRepository.findByProductVariantId(variant.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "variantId", variant.getId()));

        // 4. Check if item already in cart
        CartItem existingItem = cart.findItemByVariantId(variant.getId());

        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + request.getQuantity();

            // Validate stock
            validateStock(inventory, newQuantity);

            existingItem.updateQuantity(newQuantity);
            cartItemRepository.save(existingItem);

            log.info("Updated cart item quantity: {} -> {}",
                    existingItem.getQuantity() - request.getQuantity(), newQuantity);
        } else {
            // Add new item
            // Validate stock
            validateStock(inventory, request.getQuantity());

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .unitPrice(variant.getFinalPrice())
                    .build();

            newItem.calculateSubtotal();
            cart.addItem(newItem);
            cartItemRepository.save(newItem);

            log.info("Added new cart item: {}", variant.getSku());
        }

        // 5. Recalculate and save cart
        cart.recalculateTotals();
        cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);
        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(String sessionId, UUID variantId,
                                       UpdateCartItemRequest request) {
        log.info("Updating cart item - Session: {}, VariantId: {}, NewQuantity: {}",
                sessionId, variantId, request.getQuantity());

        // Get cart
        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ShoppingCart", "sessionId", sessionId));

        // Find item
        CartItem cartItem = cart.findItemByVariantId(variantId);
        if (cartItem == null) {
            throw new ResourceNotFoundException(
                    "CartItem", "variantId", variantId);
        }

        // If quantity is 0, remove item
        if (request.getQuantity() == 0) {
            return removeFromCart(sessionId, variantId);
        }

        // Validate stock
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "variantId", variantId));

        validateStock(inventory, request.getQuantity());

        // Update quantity
        cartItem.updateQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        // Recalculate and save cart
        cart.recalculateTotals();
        cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);
        cartRepository.save(cart);

        log.info("Cart item updated successfully");
        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(String sessionId, UUID variantId) {
        log.info("Removing item from cart - Session: {}, VariantId: {}",
                sessionId, variantId);

        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ShoppingCart", "sessionId", sessionId));

        CartItem cartItem = cart.findItemByVariantId(variantId);
        if (cartItem == null) {
            throw new ResourceNotFoundException(
                    "CartItem", "variantId", variantId);
        }

        // Remove item
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        // Recalculate and save
        cart.recalculateTotals();
        cartRepository.save(cart);

        log.info("Item removed from cart successfully");
        return mapToCartResponse(cart);
    }

    @Override
    public void clearCart(String sessionId) {
        log.info("Clearing cart - Session: {}", sessionId);

        ShoppingCart cart = cartRepository.findBySessionId(sessionId)
                .orElse(null);

        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.clear();
            cartRepository.save(cart);
            log.info("Cart cleared successfully");
        }
    }

    @Override
    public boolean validateCartStock(String sessionId) {
        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElse(null);

        if (cart == null || !cart.hasItems()) {
            return true;
        }

        return cart.validateStock();
    }

    @Override
    public int getCartItemCount(String sessionId) {
        return cartRepository.countItemsBySessionId(sessionId);
    }

    /**
     * Get or create shopping cart
     */
    private ShoppingCart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    log.info("Creating new cart for session: {}", sessionId);
                    ShoppingCart newCart = ShoppingCart.builder()
                            .sessionId(sessionId)
                            .totalAmount(BigDecimal.ZERO)
                            .totalItems(0)
                            .expiresAt(LocalDateTime.now()
                                    .plusHours(AppConstants.CART_SESSION_TIMEOUT_HOURS))
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Validate stock availability
     */
    private void validateStock(Inventory inventory, int requestedQuantity) {
        int available = inventory.getQuantityAvailable();

        if (available < requestedQuantity) {
            log.warn("Insufficient stock - VariantId: {}, Requested: {}, Available: {}",
                    inventory.getProductVariant().getId(), requestedQuantity, available);

            throw new InsufficientStockException(
                    inventory.getProductVariant().getId(),
                    requestedQuantity,
                    available
            );
        }

        log.debug("Stock validation passed - Available: {}, Requested: {}",
                available, requestedQuantity);
    }

    /**
     * Map entity to response DTO
     */
    private CartResponse mapToCartResponse(ShoppingCart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .sessionId(cart.getSessionId())
                .items(items)
                .totalItems(cart.getTotalItems())
                .totalAmount(cart.getTotalAmount())
                .expiresAt(cart.getExpiresAt())
                .build();
    }

    /**
     * Map cart item to response DTO
     */
    private CartItemResponse mapToCartItemResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        Integer availableStock = item.getAvailableStock();

        CartItemResponse response = CartItemResponse.builder()
                .cartItemId(item.getId())
                .variantId(variant.getId())
                .sku(variant.getSku())
                .productName(variant.getProduct().getName())
                .size(variant.getSize())
                .color(variant.getColor())
                .imageUrl(variant.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .availableStock(availableStock)
                .priceChanged(item.hasPriceChanged())
                .currentPrice(variant.getFinalPrice())
                .build();

        // Update stock message
        response.updateStockMessage();

        return response;
    }
}