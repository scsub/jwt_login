package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.*;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.OrderRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.OrderRequest;
import org.example.logintojwt.response.OrderResponse;
import org.example.logintojwt.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class OrderServiceUnitTest {
    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    private User user;
    private Product desk;
    private Product mouse;
    private Cart cart;
    private CartItem deskCartItem;
    private CartItem mouseCartItem;
    private Order deskOrder;
    private Order mouseOrder;
    private OrderItem deskOrderItem;
    private OrderItem mouseOrderItem;
    @BeforeEach
    void setUp() {
        Long userId = 1L;
        user = User.builder()
                .id(userId)
                .build();
        desk = Product.builder()
                .id(1L)
                .name("책상")
                .price(100L)
                .quantity(10L)
                .build();
        mouse = Product.builder()
                .id(2L)
                .name("마우스")
                .price(200L)
                .quantity(20L)
                .build();
        cart = Cart.builder()
                .id(1L)
                .user(user)
                .build();
        deskCartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(desk)
                .build();
        mouseCartItem = CartItem.builder()
                .id(2L)
                .cart(cart)
                .product(mouse)
                .build();
        deskOrder = Order.builder()
                .user(user)
                .build();
        mouseOrder = Order.builder()
                .user(user)
                .build();
        deskOrderItem = OrderItem.builder()
                .id(1L)
                .product(desk)
                .price(100L)
                .quantity(5L)
                .build();
        mouseOrderItem = OrderItem.builder()
                .id(2L)
                .product(mouse)
                .price(200L)
                .quantity(1L)
                .build();
    }

    @Test
    void createOrder() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .build();
        Product product = Product.builder()
                .id(3L)
                .price(300L)
                .quantity(30L)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .userId(user.getId())
                .build();
        Cart cart = Cart.builder()
                .id(5L)
                .user(user)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        orderService.createOrder(orderRequest);

        verify(cartItemRepository).delete(cartItem);
        verify(orderRepository).save(any(Order.class));

        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    void getAllOrders() {
        Long userId = 1L;

        cart.addCartItem(deskCartItem);
        cart.addCartItem(mouseCartItem);
        user.assignCart(cart);

        deskOrder.addOrderItem(deskOrderItem);
        mouseOrder.addOrderItem(mouseOrderItem);
        user.addOrder(deskOrder);
        user.addOrder(mouseOrder);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        List<OrderResponse> orderResponseList = orderService.getAllOrders(userId);

        verify(userRepository, times(1)).findById(userId);
        assertThat(orderResponseList.size()).isEqualTo(2);
    }

    @Test
    void getOrder() {
        Long userId = 1L;
        cart.addCartItem(deskCartItem);
        cart.addCartItem(mouseCartItem);
        user.assignCart(cart);

        deskOrder.addOrderItem(deskOrderItem);
        mouseOrder.addOrderItem(mouseOrderItem);

        user.addOrder(deskOrder);
        user.addOrder(mouseOrder);
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));

        OrderResponse orderResponse = orderService.getOrder(deskOrder.getId(), user.getId());

        verify(userRepository, times(1)).findById(userId);
        assertThat(orderResponse).isEqualTo(deskOrder.getId());
        assertThat(orderResponse.getOrderItemList()).hasSize(1);
        assertThat(orderResponse.getOrderItemList().get(0)).isEqualTo(deskOrderItem);
    }

    @Test
    void deleteOrder() {
        Long userId = 1L;
        Long orderId = deskOrder.getId();
        deskOrder.assignUser(user);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(deskOrder));
        orderService.deleteOrder(orderId, userId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository,times(1)).delete(deskOrder);
    }
}