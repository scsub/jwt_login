package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.*;
import org.example.logintojwt.repository.CartItemRepository;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.CartItemQuantityRequest;
import org.example.logintojwt.request.CartItemRequest;
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

    public void addItemInCart(Long userId, CartItemRequest cartItemRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("id","유저를 찾을수 없음"));
        user.getCart().getCartItems().forEach(
                (cartItem) -> {
                    if (cartItem.getProduct().getId().equals(cartItemRequest.getProductId())) {
                        throw new DuplicatedProductException("product","장바구에 이미 있는 상품입니다");
                    }
                });

        Product product = productRepository.findById(cartItemRequest.getProductId()).orElseThrow(() -> new ProductNotFoundException("id","상품을 찾을수없음"));
        if(product.getQuantity()<cartItemRequest.getQuantity()){
            throw new OutOfStockException("quantity", "재고량 보다 많이 구매할수없습니다");
        }

        Cart cart = user.getCart();

        CartItem cartItem = CartItem.builder()
                .quantity(cartItemRequest.getQuantity())
                .product(product)
                .cart(cart)
                .build();

        cartItemRepository.save(cartItem);

        cart.addCartItem(cartItem);
        cartRepository.save(cart);
    }

    public CartResponse getCartByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("id","유저를 찾을수 없음"));
        Cart cart = user.getCart();
        return CartResponse.from(cart);
    }

    public void removeItemFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CartItemNotFoundException("id","아이템을 카트에서 찾을수없음"));
        cartItem.getCart().removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    public void changeQuantity(Long cartItemId,Long quantity, Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("user","유저를 찾을수 없음"));
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() -> new CartItemNotFoundException("id","아이템을 카트에서 찾을수없음"));
        if (user.getCart().getCartItems().contains(cartItem)) {
            cartItem.changeQuantity(quantity);
        }
    }
}
