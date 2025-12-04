package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.ProductImage;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private Long quantity;
    private Long id;
    private Long productId;
    private String productName;
    private Long productPrice;
    private ProductImageResponse productImageResponse;

    public static CartItemResponse from(CartItem cartItem) {
        List<ProductImage> productImageList = cartItem.getProduct().getProductImageList();
        ProductImageResponse productImageResponse = new ProductImageResponse();
        if (productImageList!=null && !productImageList.isEmpty()) {
            productImageResponse = ProductImageResponse.from(productImageList.get(0));
        }

        return CartItemResponse.builder()
                .quantity(cartItem.getQuantity())
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productPrice(cartItem.getProduct().getPrice())
                .productImageResponse(productImageResponse)
                .build();
    }

    public static List<CartItemResponse> fromList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItem -> CartItemResponse.from(cartItem))
                .collect(Collectors.toList());
    }
}
