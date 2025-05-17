package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.OrderRequest;
import org.example.logintojwt.response.OrderResponse;
import org.example.logintojwt.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        orderService.createOrder(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<OrderResponse> orderResponseList = orderService.getAllOrders(userId);
        ResponseEntity.ok().body(orderResponseList);
        return ResponseEntity.status(HttpStatus.OK).body(orderResponseList);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrders(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long orderId) {
        Long userId = userDetails.getId();
        OrderResponse orderResponse = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok().body(orderResponse);
    }
    @PatchMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long orderId) {
        Long userId = userDetails.getId();
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long orderId) {
        Long userId = userDetails.getId();
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.noContent().build();
    }
}
