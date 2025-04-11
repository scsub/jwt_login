package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.CartItemNotFoundException;
import org.example.logintojwt.exception.ProductNotFoundException;
import org.example.logintojwt.exception.UserNotFoundException;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.CartRequest;
import org.example.logintojwt.response.CartResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void addItemInCart(Long userId, CartRequest cartRequest) {
        Product product = productRepository.findById(cartRequest.getProductId()).orElseThrow(() -> new ProductNotFoundException("상품을 찾을수없음"));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("유저를 찾을수 없음"));
        Cart cart = user.getCart();

        CartItem cartItem = CartItem.builder()
                .quantity(cartRequest.getQuantity())
                .product(product)
                .cart(cart)
                .build();

        cartItemRepository.save(cartItem);

        cart.addCartItem(cartItem);
        cartRepository.save(cart);
    }

    public CartResponse getCartByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("유저를 찾을수 없음"));
        Cart cart = user.getCart();
        return CartResponse.from(cart);
    }

    public void removeItemFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CartItemNotFoundException("아이템을 카트에서 찾을수없음"));
        cartItem.getCart().removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    public void changeQuantity(Long cartItemId, Long quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CartItemNotFoundException("아이템을 카트에서 찾을수없음"));
        cartItem.changeQuantity(quantity);
    }
}
