package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.*;
import org.example.logintojwt.exception.UserNotFoundException;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.OrderRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.response.OrderResponse;
import org.example.logintojwt.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    private Order order;
    private Order deskOrder;
    private Order mouseOrder;
    private OrderItem deskOrderItem;
    private OrderItem mouseOrderItem;
    private Cart emptyCart;
    @BeforeEach
    void setUp() {
        // 유저와 카트를 만들고 카드안에 상품 2개가 들어있는 상태
        user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        cart = Cart.builder()
                .id(1L)
                .user(user)
                .build(); // cartItems 자동생성
        emptyCart = Cart.builder()
                .id(2L)
                .user(user)
                .build(); // cartItems 자동생성
        // 상품
        desk = Product.builder()
                .id(1L)
                .name("책상")
                .description("빨간 책상")
                .price(100L)
                .quantity(10L)
                .category(null)
                .build();
        mouse = Product.builder()
                .id(2L)
                .name("마우스")
                .description("주황 마우스")
                .price(200L)
                .quantity(20L)
                .category(null)
                .build();
        // 장바구니 아이템
        deskCartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(desk)
                .quantity(10L)
                .build();
        mouseCartItem = CartItem.builder()
                .id(2L)
                .cart(cart)
                .product(mouse)
                .quantity(20L)
                .build();
        deskOrder = Order.builder()
                .id(2L)
                .user(user)
                .orderStatus(OrderStatus.ORDERED)
                .build();
        mouseOrder = Order.builder()
                .id(3L)
                .user(user)
                .orderStatus(OrderStatus.ORDERED)
                .build();
        deskOrderItem = OrderItem.builder()
                .id(1L)
                .order(deskOrder)
                .product(desk)
                .price(100L)
                .quantity(10L)
                .build();
        mouseOrderItem = OrderItem.builder()
                .id(2L)
                .order(mouseOrder)
                .product(mouse)
                .price(200L)
                .quantity(20L)
                .build();
        user.assignCart(cart);
        cart.addCartItem(deskCartItem);
        cart.addCartItem(mouseCartItem);
        deskOrder.addOrderItem(deskOrderItem);
        mouseOrder.addOrderItem(mouseOrderItem);
    }

    @Test
    @DisplayName("주문성공")
    void createOrder() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //실행
        orderService.createOrder(user.getId());
        // 단순 검증
        verify(userRepository, times(1)).findById(user.getId());
        verify(cartItemRepository,times(1)).delete(deskCartItem);
        verify(cartItemRepository,times(1)).delete(mouseCartItem);
        // 오더 캡쳐
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order orderCaptorValue = orderCaptor.getValue();

        assertThat(orderCaptorValue.getUser()).isEqualTo(user);
        assertThat(orderCaptorValue.getOrderItemList()).hasSize(2);

        assertThat(mouse.getQuantity()).isEqualTo(0L);
        assertThat(desk.getQuantity()).isEqualTo(0L);
        assertThat(cart.getCartItems()).isEmpty();
    }
    @Test
    @DisplayName("재고 부족해서 주문실패")
    void create_order_out_of_stock() {
        desk.updateQuantity(1L); // 책상 재고를 낮춤
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //
        assertThatThrownBy(() -> orderService.createOrder(user.getId())).isInstanceOf(IllegalStateException.class);
        //
        verify(userRepository, times(1)).findById(user.getId());
        verify(cartItemRepository,never()).delete(any(CartItem.class));
        verify(orderRepository, never()).save(any(Order.class));

    }
    @Test
    @DisplayName("주문 실패 유저를 찾을수없음")
    void create_order_user_not_found() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> orderService.createOrder(user.getId())).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(user.getId());
        verify(cartItemRepository,never()).delete(any(CartItem.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("장바구니에 아이템없어서 주문실패")
    void create_order_no_item_in_cart(){
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        user.assignCart(emptyCart); // 빈 카드 적용
        // 실행
        assertThatThrownBy(() -> orderService.createOrder(user.getId())).isInstanceOf(IllegalStateException.class);
        // 검증
        verify(userRepository, times(1)).findById(user.getId());
        verify(cartItemRepository,never()).delete(any(CartItem.class));
        verify(cartItemRepository,never()).delete(any(CartItem.class));
        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    @DisplayName("유저의 주문 가져오기")
    void getAllOrders() {
        user.addOrder(deskOrder);
        user.addOrder(mouseOrder);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // 실행
        List<OrderResponse> orderResponseList = orderService.getAllOrders(user.getId());

        verify(userRepository, times(1)).findById(user.getId());
        assertThat(orderResponseList.size()).isEqualTo(2);
    }
    @Test
    @DisplayName("유저의 주문 가져오기 실패 유저 없음")
    void getAllOrders_user_not_found() {
        user.addOrder(deskOrder);
        user.addOrder(mouseOrder);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> orderService.getAllOrders(user.getId()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(user.getId());
    }

    @Test
    @DisplayName("orderId와 userId로 주문 하나 가져오기")
    void getOrder() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));

        OrderResponse orderResponse = orderService.getOrder(deskOrder.getId(), user.getId());

        verify(orderRepository, times(1)).findById(deskOrder.getId());
        assertThat(orderResponse.getUserId()).isEqualTo(user.getId());
        assertThat(orderResponse.getItemList().get(0).getProductName()).isEqualTo(desk.getName());
    }

    @Test
    @DisplayName("주문가져오려는데 해당 주문이없음")
    void getOrder_order_not_found() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> orderService.getOrder(deskOrder.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderRepository, times(1)).findById(deskOrder.getId());
    }

    @Test
    @DisplayName("주문은 있으나 유저가 해당유저가 아님")
    void getOrder_user_not_found() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));
        // 실행
        assertThatThrownBy(() -> orderService.getOrder(deskOrder.getId(), 999L))
                .isInstanceOf(SecurityException.class);

        verify(orderRepository, times(1)).findById(deskOrder.getId());
    }

    @Test
    @DisplayName("유저의 주문 삭제")
    void deleteOrder() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));
        // 실행
        orderService.deleteOrder(deskOrder.getId(), user.getId());
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        verify(orderRepository,times(1)).delete(deskOrder);
    }

    @Test
    @DisplayName("유저 주문 삭제 불가 주문을 찾을수없음")
    void deleteOrder_order_not_found() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> orderService.deleteOrder(deskOrder.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        verify(orderRepository, never()).delete(deskOrder);
    }

    @Test
    @DisplayName("유저 주문 삭제 불가 유저 아이디로 유저를 찾을수없음")
    void deleteOrder_user_not_found() {
        Long notUserId = 999L;
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));
        // 실행
        assertThatThrownBy(() -> orderService.deleteOrder(deskOrder.getId(), notUserId))
                .isInstanceOf(SecurityException.class);
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        verify(orderRepository, never()).delete(deskOrder);
    }
    @Test
    @DisplayName("주문 취소해서 DB에 ORDERED가 아닌 CANCELED로 바꾸기")
    void cancelOrder() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));
        // 실행
        orderService.cancelOrder(deskOrder.getId(), user.getId());
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        assertThat(deskOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(desk.getQuantity()).isEqualTo(20L); // 기본10개 있고 주문들어간게 10개이니 취소되면서 20개가된다
    }

    @Test
    @DisplayName("주문 취소 불가 주문을 찾을수없음")
    void cancel_order_order_not_found() {
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> orderService.cancelOrder(deskOrder.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        assertThat(deskOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(desk.getQuantity()).isEqualTo(10L); // 주문 취소를 못해서 상품10개 그대로
    }

    @Test
    @DisplayName("주문 취소 불가 유저 아이디로 유저를 찾을수없음")
    void cancel_order_user_not_found() {
        Long notUserId = 999L;
        when(orderRepository.findById(deskOrder.getId())).thenReturn(Optional.of(deskOrder));
        // 실행
        assertThatThrownBy(() -> orderService.cancelOrder(deskOrder.getId(), notUserId))
                .isInstanceOf(SecurityException.class);
        // 검증
        verify(orderRepository, times(1)).findById(deskOrder.getId());
        assertThat(deskOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(desk.getQuantity()).isEqualTo(10L); // 주문 취소를 못해서 상품10개 그대로
    }
}