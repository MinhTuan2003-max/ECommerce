package fpt.tuanhm43.server.controllers;

import fpt.tuanhm43.server.dtos.ApiResponseDTO;
import fpt.tuanhm43.server.dtos.cart.AddToCartRequest;
import fpt.tuanhm43.server.entities.CartItem;
import fpt.tuanhm43.server.entities.User;
import fpt.tuanhm43.server.exceptions.ResourceNotFoundException;
import fpt.tuanhm43.server.repositories.ProductRepository;
import fpt.tuanhm43.server.repositories.CartItemRepository;
import fpt.tuanhm43.server.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private UUID currentUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    Object principal = auth.getPrincipal();

                    String email;
                    if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
                        if (principal instanceof String s) {
                            email = s;
                        } else {
                            email = String.valueOf(principal);
                        }
                    } else {
                        email = userDetails.getUsername();
                    }

                    return email;
                })
                .flatMap(userRepository::findByEmail)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Unauthorized: User session is invalid or not found"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Map<String, Object>>>> getCart() {
        UUID userId = currentUserId();
        List<Map<String, Object>> items = cartItemRepository.findByUserId(userId).stream()
                .map(ci -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", ci.getId());
                    m.put("productId", ci.getId());
                    m.put("quantity", ci.getQuantity());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDTO.success(items));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> addToCart(@Valid @RequestBody AddToCartRequest req) {
        UUID userId = currentUserId();
        productRepository.findById(req.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        var user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CartItem ci = CartItem.builder()
                .user(user)
                .id(req.getProductId())
                .quantity(req.getQuantity())
                .build();

        CartItem saved = cartItemRepository.save(ci);
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("productId", saved.getId());
        resp.put("quantity", saved.getQuantity());
        return ResponseEntity.ok(ApiResponseDTO.success(resp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> remove(@PathVariable("id") UUID id) {
        UUID userId = currentUserId();
        cartItemRepository.deleteByIdAndUserId(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
}
