package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Cart;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> cartItemResponses;

    public static CartResponse from(Cart cart) {
        return CartResponse.builder()
                .userId(cart.getUser().getId())
                .cartItemResponses(CartItemResponse.fromList(cart.getCartItems()))
                .build();
    }
}
