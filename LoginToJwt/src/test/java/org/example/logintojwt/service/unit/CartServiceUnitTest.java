package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.CartItemRequest;
import org.example.logintojwt.response.CartResponse;
import org.example.logintojwt.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CartServiceUnitTest {
    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private  CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private  UserRepository userRepository;
    @Test
    void addItemInCart() {
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .username("김가나")
                .build();
        Product product = Product.builder()
                .id(10L)
                .name("스마트폰")
                .build();
        Cart cart = Cart.builder()
                .user(user)
                .build();
        user.assignCart(cart);

        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(10L)
                .quantity(5L)
                .build();

        CartItem cartItem = CartItem.builder()
                .id(3L)
                .cart(cart)
                .product(product)
                .quantity(5L)
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.ofNullable(product));
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));

        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        // CartItem도 마찬가지
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        cartService.addItemInCart(userId, cartItemRequest);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCartByUserId() {
        Long userId = 1L;


        User user = User.builder()
                .id(userId)
                .username("이름")
                .build();

        Cart cart = Cart.builder()
                .user(user)
                .build();

        user.assignCart(cart);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        CartResponse cartResponse = cartService.getCartByUserId(userId);

        verify(userRepository, times(1)).findById(userId);
        assertThat(cartResponse.getUserId()).isEqualTo(userId);
    }

    @Test
    void removeItemFromCart() {
        Long cartItemId = 1L;
        Product product = Product.builder()
                .name("pro")
                .build();
        Cart cart = Cart.builder()
                .build();
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .build();


        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        cartService.removeItemFromCart(1L);

        verify(cartItemRepository, times(1)).findById(cartItemId);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    void changeQuantity() {
        Long cartItemId = 3L;
        CartItem cartItem = CartItem.builder()
                .build();
        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        cartService.changeQuantity(cartItemId, 5L, 1L);

        verify(cartItemRepository, times(1)).findById(cartItemId);
    }
}