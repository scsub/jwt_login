package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.*;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.CartItemRequest;
import org.example.logintojwt.response.CartResponse;
import org.example.logintojwt.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    @DisplayName("카드에 상품추가")
    void addItemInCart() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        // 실행
        cartService.addItemInCart(user.getId(), cartItemRequest);
        // 검증
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(1)).save(cartItemCaptor.capture());
        CartItem savedCartItem = cartItemCaptor.getValue();
        assertThat(savedCartItem.getProduct()).isEqualTo(product);
        assertThat(savedCartItem.getCart()).isEqualTo(cart);
        assertThat(savedCartItem.getProduct().getName()).isEqualTo(product.getName());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, times(1)).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getCartItems()).hasSize(1);
        assertThat(savedCart.getCartItems().get(0).getProduct()).isEqualTo(product);
    }

    @Test
    @DisplayName("유저가 없어서 카트에 상품추가 실패")
    void add_item_in_cart_userNotFound() {
        Long userId = 1L;
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> cartService.addItemInCart(userId, cartItemRequest))
                .isInstanceOf(UserNotFoundException.class);
        // 검증
        verify(productRepository, never()).findById(product.getId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니에 이미 있는 상품")
    void add_item_in_cart_duplicate() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem duplicateItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(duplicateItem);
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //실행
        assertThatThrownBy(() -> cartService.addItemInCart(user.getId(), cartItemRequest))
                .isInstanceOf(DuplicatedProductException.class);

        verify(userRepository).findById(user.getId());
        verify(productRepository,never()).findById(anyLong());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("상품 담으려고하는데 상품이 없음")
    void add_item_in_cart_product_not_found() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> cartService.addItemInCart(user.getId(), cartItemRequest))
                .isInstanceOf(ProductNotFoundException.class);

        verify(userRepository).findById(user.getId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("상품 갯수를 초과해서 장바구니에 담기")
    void add_item_in_cart_exceed_number(){
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(101L)
                .build();
        user.assignCart(cart);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        // 실행
        assertThatThrownBy(() -> cartService.addItemInCart(user.getId(), cartItemRequest))
                .isInstanceOf(OutOfStockException.class);
        // 검증
        verify(userRepository).findById(user.getId());
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("userId로 카트 찾아오기")
    void get_cart_by_userId() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        // 실행
        CartResponse cartResponse = cartService.getCartByUserId(user.getId());
        // 검증
        verify(userRepository, times(1)).findById(user.getId());
        assertThat(cartResponse.getUserId()).isEqualTo(user.getId());
        assertThat(cartResponse.getCartItemResponses().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("카트 가져오려는데 유저가없음")
    void get_cart_by_userId_user_not_found() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> cartService.getCartByUserId(userId)).isInstanceOf(UserNotFoundException.class);
        // 검증
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("카트에서 아이템삭제 성공")
    void removeItemFromCart() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);

        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        cartService.removeItemFromCart(cartItem.getId());

        verify(cartItemRepository, times(1)).findById(cartItem.getId());
        ArgumentCaptor<CartItem> cartItemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository, times(1)).delete(cartItemCaptor.capture());
        assertThat(cartItemCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(cartItemCaptor.getValue().getQuantity()).isEqualTo(1L);
        assertThat(cart.getCartItems()).doesNotContain(cartItem);
    }

    @Test
    @DisplayName("상품아이디가 잘못되서 삭제 실패")
    void remove_item_wrong_product_id(){
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);

        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.empty());
        // 실행
        assertThatThrownBy(() -> cartService.removeItemFromCart(cartItem.getId()))
                .isInstanceOf(CartItemNotFoundException.class);
        // 검증
        verify(cartItemRepository).findById(cartItem.getId());
        verify(cartItemRepository,never()).delete(cartItem);
    }

    @Test
    @DisplayName("장바구니 아이템 갯수 변경")
    void change_cart_item_quantity() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);

        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        //실행
        cartService.changeQuantity(cartItem.getId(), 5L, user.getId());
        //검증
        assertThat(cartItem.getQuantity()).isEqualTo(5L);
        verify(cartItemRepository, times(1)).findById(cartItem.getId());
    }

    @Test
    @DisplayName("장바구니 아이템 갯수 변경 실패 유저를 못찾음")
    void change_cart_item_quantity_user_not_found() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        //실행
        assertThatThrownBy(() -> cartService.changeQuantity(cartItem.getId(), 5L, user.getId()))
                .isInstanceOf(UserNotFoundException.class);
        //검증
        verify(cartItemRepository,never()).findById(anyLong());
        assertThat(cartItem.getQuantity()).isEqualTo(1L);
    }

    @Test
    @DisplayName("장바구니 아이템 갯수 실패 카트아이템을 못찾음")
    void change_cart_item_quantity_cart_item_not_found() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem cartItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
        cart.addCartItem(cartItem);
        user.assignCart(cart);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        //실행
        assertThatThrownBy(() -> cartService.changeQuantity(cartItem.getId(), 5L, user.getId()))
                .isInstanceOf(CartItemNotFoundException.class);
        //검증
        verify(userRepository).findById(user.getId());
        assertThat(cartItem.getQuantity()).isEqualTo(1L);
    }



    private void setup() {
        User user = User.builder()
                .id(1L)
                .username("requestUsername")
                .password("encodedPassword")
                .address("requestAddress")
                .phoneNumber("01087654321")
                .email("request@gmail.com")
                .build();
        Product product = Product.builder()
                .id(1L)
                .name("스마트폰")
                .description("삼성에서 제조")
                .price(1000000L)
                .quantity(100L)
                .category(null)
                .build();// reviewList, productImageList 자동생성
        Cart cart = Cart.builder()
                .user(user)
                .build(); // cartItems 자동생성
        CartItemRequest cartItemRequest = CartItemRequest.builder()
                .productId(product.getId())
                .quantity(100L)
                .build();
        CartItem duplicateItem = CartItem.builder()
                .id(1L)
                .cart(cart)
                .product(product)
                .quantity(1L)
                .build();
    }
}