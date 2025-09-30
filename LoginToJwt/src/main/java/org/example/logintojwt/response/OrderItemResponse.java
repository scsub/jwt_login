package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Order;
import org.example.logintojwt.entity.OrderItem;

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
                .map(orderItem -> OrderItemResponse.builder()
                        .orderId(orderItem.getOrder().getId())
                        .productName(orderItem.getProduct().getName())
                        .productId(orderItem.getProduct().getId())
                        .quantity(orderItem.getQuantity())
                        .price(orderItem.getPrice())
                        .productImageResponse(ProductImageResponse.from(orderItem.getProduct().getProductImageList().get(0)))
                        .build()).toList();
    }
}
