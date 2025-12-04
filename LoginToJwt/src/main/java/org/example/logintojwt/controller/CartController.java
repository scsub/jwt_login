package org.example.logintojwt.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.CartItemQuantityRequest;
import org.example.logintojwt.request.CartItemRequest;
import org.example.logintojwt.response.CartResponse;
import org.example.logintojwt.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    private ResponseEntity<?> addItemInCart(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody CartItemRequest cartItemRequest) {
        Long userId = userDetails.getId();
        cartService.addItemInCart(userId, cartItemRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    private ResponseEntity<?> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        CartResponse cartResponse = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/items/{cartItemId}")
    private ResponseEntity<?> removeItemFromCart(@PathVariable Long cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{cartItemId}")
    private ResponseEntity<?> changeItemQuantity(@PathVariable Long cartItemId,
                                                 @RequestBody @Valid CartItemQuantityRequest cartItemQuantityRequest,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        cartService.changeQuantity(cartItemId, cartItemQuantityRequest.getQuantity(), userId);
        return ResponseEntity.ok().build();
    }
}
