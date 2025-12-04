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
import org.example.logintojwt.testcontainersTest.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IntegrationTestBase.class)
public class CartServiceIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    void init() {
        setup();
    }
    private void cleanUp(){
        cartRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();
    }
    private void setup() {
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
    @DisplayName("로그인된 유저가 장바구니에 상품 추가")
    void addItemInCart() throws Exception {
    }
}
