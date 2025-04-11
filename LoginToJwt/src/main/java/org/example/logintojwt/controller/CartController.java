package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.CartRequest;
import org.example.logintojwt.response.CartResponse;
import org.example.logintojwt.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    private ResponseEntity<?> addItemInCart(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CartRequest cartRequest) {
        Long userId = userDetails.getId();
        cartService.addItemInCart(userId,cartRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    private ResponseEntity<?> getCart(@PathVariable Long userId) {
        CartResponse cartResponse = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/items/{cartItemId}")
    private ResponseEntity<?> removeItemFromCart(@PathVariable Long cartItemId) {
        cartService.removeItemFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{cartItemId}")
    private ResponseEntity<?> changeItemQuantity(@PathVariable Long cartItemId, @RequestBody CartRequest cartRequest) {
        cartService.changeQuantity(cartItemId, cartRequest.getQuantity());
        return ResponseEntity.ok().build();
    }
}
