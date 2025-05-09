package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Order;
import org.example.logintojwt.entity.OrderItem;
import org.example.logintojwt.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private List<OrderItemResponse> orderItemList;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderItemList(OrderItemResponse.fromList(order.getOrderItemList()))
                .orderDate(order.getOrderDate())
                .orderStatus(order.getOrderStatus())
                .build();
    }
}
