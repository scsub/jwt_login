package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.*;
import org.example.logintojwt.exception.UserNotFoundException;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.OrderRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.OrderRequest;
import org.example.logintojwt.response.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public void createOrder(OrderRequest orderRequest) {
        Long userId = orderRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("유저를 찾을수 없음"));
        Cart cart = user.getCart();
        if (cart == null) {
            throw new IllegalStateException("장바구니에 상품을 채워주세요");
        }
        Order order = Order.builder()
                .user(user)
                .orderStatus(OrderStatus.ORDERED)
                .build();
        //카트에있는 아이템을 오더 아이템으로 바꾼후 오더에 넣기
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.addOrderItem(orderItem);
            cartItemRepository.delete(cartItem);
        }
        orderRepository.save(order);
        cart.getCartItems().clear();
    }

    public List<OrderResponse> getAllOrders(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("유저를 찾을수 없음"));
        List<Order> orderList = user.getOrderList();
        return orderList.stream()
                .map(order -> OrderResponse.from(order))
                .collect(Collectors.toList());
    }

    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을수없음"));
        if (order.getUser().getId().equals(userId)) {
            return OrderResponse.from(order);
        }else {
            throw new SecurityException("주문자 정보가 일치하지 않습니다");
        }
    }

    public void deleteOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("주문을 찾을수없음"));
        if (order.getUser().getId().equals(userId)) {
            orderRepository.delete(order);
        }else {
            throw new SecurityException("주문자 정보가 일치하지 않습니다");
        }
    }
}
