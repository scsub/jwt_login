package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private Long quantity;
    private Long cartId;
    private Long productId;

    public static CartItemResponse from(CartItem cartItem) {
        return CartItemResponse.builder()
                .quantity(cartItem.getQuantity())
                .cartId(cartItem.getId())
                .productId(cartItem.getProduct().getId()).build();
    }

    public static List<CartItemResponse> fromList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> CartItemResponse.from(cartItem))
                .collect(Collectors.toList());
    }
}
