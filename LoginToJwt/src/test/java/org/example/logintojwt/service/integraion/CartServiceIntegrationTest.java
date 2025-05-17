package org.example.logintojwt.service.integraion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logintojwt.entity.Cart;
import org.example.logintojwt.entity.CartItem;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.repository.CartRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.CartItemRequest;
import org.example.logintojwt.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 db로 자동 교체
@ExtendWith(SpringExtension.class)
public class CartServiceIntegrationTest {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Product product;
    @BeforeEach
    public void setup() {
        user = User.builder()
                .username("abcdef")
                .password("123456")
                .phoneNumber("01012345678")
                .email("user@gmail.com")
                .address("대전")
                .build();
        Cart.builder()
                .build();
        product = Product.builder()
                .name("가구")
                .price(10000L)
                .quantity(10L)
                .build();
        CartItem.builder()
                .build();
        userRepository.save(user);
        productRepository.save(product);
    }

    @Test
    void addItemInCart() throws Exception {
        CartItemRequest.builder()
                .productId(product.getId())
                .quantity(10L)
                .build();

        //CartItemRequest.builder()
        //objectMapper.writeValueAsString()
    }
}
