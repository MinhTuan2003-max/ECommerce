package fpt.tuanhm43.server.services.impl;

import fpt.tuanhm43.server.constants.AppConstants;
import fpt.tuanhm43.server.dtos.cart.request.AddToCartRequest;
import fpt.tuanhm43.server.dtos.cart.request.UpdateCartItemRequest;
import fpt.tuanhm43.server.dtos.cart.response.CartResponse;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.entities.Inventory;
import fpt.tuanhm43.server.entities.ProductVariant;
import fpt.tuanhm43.server.entities.ShoppingCart;
import fpt.tuanhm43.server.exceptions.InsufficientStockException;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.mappers.CartMapper;
import fpt.tuanhm43.server.repositories.CartItemRepository;
import fpt.tuanhm43.server.repositories.InventoryRepository;
import fpt.tuanhm43.server.repositories.ProductVariantRepository;
import fpt.tuanhm43.server.repositories.ShoppingCartRepository;
import fpt.tuanhm43.server.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getBySessionId(String sessionId) {
        log.debug("Getting cart for session: {}", sessionId);

        return cartRepository.findBySessionIdWithItems(sessionId)
                .map(cart -> {
                    cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);
                    cartRepository.save(cart);
                    return cartMapper.toCartResponse(cart);
                })
                .orElse(CartResponse.empty(sessionId));
    }

    @Override
    public CartResponse addToCart(String sessionId, AddToCartRequest request) {
        log.info("Adding item to cart - Session: {}, VariantId: {}, Quantity: {}",
                sessionId, request.getVariantId(), request.getQuantity());

        ShoppingCart cart = getOrCreateCart(sessionId);

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", "id", request.getVariantId()));

        Inventory inventory = inventoryRepository.findByProductVariantId(variant.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "variantId", variant.getId()));

        CartItem existingItem = cart.findItemByVariantId(variant.getId());

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            validateStock(inventory, newQuantity);
            existingItem.updateQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
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
        }

        cart.recalculateTotals();
        cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);

        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse update(String sessionId, UUID variantId, UpdateCartItemRequest request) {
        log.info("Updating cart item - Session: {}, VariantId: {}, NewQuantity: {}",
                sessionId, variantId, request.getQuantity());

        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart", "sessionId", sessionId));

        CartItem cartItem = cart.findItemByVariantId(variantId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "variantId", variantId);
        }

        if (request.getQuantity() == 0) {
            return remove(sessionId, variantId);
        }

        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "variantId", variantId));

        validateStock(inventory, request.getQuantity());

        cartItem.updateQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        cart.recalculateTotals();
        cart.extendExpiration(AppConstants.CART_SESSION_TIMEOUT_HOURS);

        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse remove(String sessionId, UUID variantId) {
        log.info("Removing item from cart - Session: {}, VariantId: {}", sessionId, variantId);

        ShoppingCart cart = cartRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart", "sessionId", sessionId));

        CartItem cartItem = cart.findItemByVariantId(variantId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("CartItem", "variantId", variantId);
        }

        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.recalculateTotals();
        return cartMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    public void clear(String sessionId) {
        log.info("Clearing cart - Session: {}", sessionId);
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.clear();
            cartRepository.save(cart);
        });
    }

    @Override
    public boolean validateCartStock(String sessionId) {
        return cartRepository.findBySessionIdWithItems(sessionId)
                .map(ShoppingCart::validateStock)
                .orElse(true);
    }

    @Override
    public int getCartItemCount(String sessionId) {
        return cartRepository.countItemsBySessionId(sessionId);
    }

    private ShoppingCart getOrCreateCart(String sessionId) {
        return cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    log.info("Creating new cart for session: {}", sessionId);
                    return cartRepository.save(ShoppingCart.builder()
                            .sessionId(sessionId)
                            .totalAmount(BigDecimal.ZERO)
                            .totalItems(0)
                            .expiresAt(LocalDateTime.now().plusHours(AppConstants.CART_SESSION_TIMEOUT_HOURS))
                            .build());
                });
    }

    private void validateStock(Inventory inventory, int requestedQuantity) {
        int available = inventory.getQuantityAvailable();
        if (available < requestedQuantity) {
            throw new InsufficientStockException(
                    inventory.getProductVariant().getId(), requestedQuantity, available);
        }
    }
}