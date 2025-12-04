package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Order;
import org.example.logintojwt.entity.OrderItem;
import org.example.logintojwt.entity.ProductImage;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderItemResponse {
    private Long orderId;
    private String productName;
    private Long productId;
    private Long quantity;
    private Long price;
    private ProductImageResponse productImageResponse;


    public static List<OrderItemResponse> fromList(List<OrderItem> orderItemList) {


        return orderItemList.stream()
                .map(orderItem -> {
                    List<ProductImage> productImageList = orderItem.getProduct().getProductImageList();
                    ProductImageResponse productImageResponse = new ProductImageResponse();
                    if (productImageList != null && !productImageList.isEmpty()) {
                        productImageResponse = ProductImageResponse.from(productImageList.get(0));
                    }
                    return OrderItemResponse.builder()
                            .orderId(orderItem.getOrder().getId())
                            .productName(orderItem.getProduct().getName())
                            .productId(orderItem.getProduct().getId())
                            .quantity(orderItem.getQuantity())
                            .price(orderItem.getPrice())
                            .productImageResponse(productImageResponse)
                            .build();
                }).toList();
    }
}
