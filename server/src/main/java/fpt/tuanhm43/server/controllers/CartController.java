package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.cart.AddToCartRequest;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.cart.CartItemRepository;
import fpt.tuanhm43.server.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        Object principal = auth.getPrincipal();
        String username = principal instanceof org.springframework.security.core.userdetails.User u
                ? u.getUsername()
                : principal.toString();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getCart() {
        Long userId = currentUserId();
        List<Map<String, Object>> items = cartItemRepository.findByUserId(userId).stream()
                .map(ci -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", ci.getId());
                    m.put("productId", ci.getProductId());
                    m.put("quantity", ci.getQuantity());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDTO.success(items));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addToCart(@Valid @RequestBody AddToCartRequest req) {
        Long userId = currentUserId();
        // ensure product exists
        productRepository.findById(req.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CartItem ci = CartItem.builder()
                .user(user)
                .productId(req.getProductId())
                .quantity(req.getQuantity())
                .build();

        CartItem saved = cartItemRepository.save(ci);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("productId", saved.getProductId());
        resp.put("quantity", saved.getQuantity());
        return ResponseEntity.ok(ApiResponseDTO.success(resp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> remove(@PathVariable("id") Long id) {
        Long userId = currentUserId();
        cartItemRepository.deleteByIdAndUserId(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
}
