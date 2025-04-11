package org.example.logintojwt.service.integraion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logintojwt.entity.*;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.ReviewRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.ReviewRequest;
import org.example.logintojwt.service.ReviewService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@Rollback
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 db로 자동 교체
@ExtendWith(SpringExtension.class)
public class ReviewIntegrationTest {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User miko;
    private User noel;

    private Product desk;
    private Product chair;

    private Review mikoDeskReview;
    private Review mikoChairReview;
    private Review noelDeskReview;
    private Review noelChairReview;

    private Category furniture;
    @BeforeEach
    void setUp() {
        furniture = Category.builder()
                .name("Furniture")
                .parent(null)
                .build();

        miko = User.builder()
                .username("miko")
                .email("miko@example.com")
                .phoneNumber("01011111111")
                .address("dioStreet")
                .roles(List.of(Role.ROLE_USER))
                .password(passwordEncoder.encode("3150"))
                .build();

        noel = User.builder()
                .username("noel")
                .email("noel@example.com")
                .phoneNumber("01022222222")
                .password(passwordEncoder.encode("3151"))
                .address("jojoStreet")
                .roles(List.of(Role.ROLE_USER))
                .build();

        desk = Product.builder()
                .name("desk")
                .price(1000L)
                .description("책상")
                .quantity(1L)
                .category(furniture)
                .build();

        chair = Product.builder()
                .name("chair")
                .price(2000L)
                .description("의자")
                .quantity(2L)
                .category(furniture)
                .build();

        mikoDeskReview = Review.builder()
                .product(desk)
                .user(miko)
                .content("4점 책상")
                .rating(4L)
                .build();
        mikoChairReview = Review.builder()
                .product(chair)
                .user(miko)
                .content("4점 의자")
                .rating(4L)
                .build();
        noelDeskReview = Review.builder()
                .product(desk)
                .user(noel)
                .content("3점 책상")
                .rating(3L)
                .build();

        noelChairReview = Review.builder()
                .product(chair)
                .user(noel)
                .content("3점 의자")
                .rating(3L)
                .build();
        userRepository.save(miko);
        userRepository.save(noel);
        categoryRepository.save(furniture);
        productRepository.save(desk);
        productRepository.save(chair);
        reviewRepository.save(mikoDeskReview);
        reviewRepository.save(mikoChairReview);
        reviewRepository.save(noelDeskReview);
        reviewRepository.save(noelChairReview);
    }
    @Test
    void createReview() throws Exception {
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .userId(miko.getId())
                .productId(desk.getId())
                .rating(5L)
                .content("좋은 책상")
                .build();
        String json = objectMapper.writeValueAsString(reviewRequest);
        mvc.perform(post("/api/reviews")
                        .with(SecurityMockMvcRequestPostProcessors.user("miko").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("좋은 책상"))
                .andDo(print());

        List<Review> reviewList = reviewRepository.findByUserId(miko.getId());
        assertThat(reviewList.size()).isEqualTo(3);
        assertThat(reviewList.get(2).getProduct()).isEqualTo(desk);
        assertThat(reviewList.get(2).getRating()).isEqualTo(5L);
    }

    @Test
    void findReviewByProductId() throws Exception {
        Long productId =chair.getId();

        mvc.perform(get("/api/reviews/product/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("4점 의자"))
                .andExpect(jsonPath("$[1].content").value("3점 의자"))
                .andDo(print());
    }

    @Test
    void findReviewByUserId() throws Exception {
        Long userId = miko.getId();

        mvc.perform(get("/api/reviews/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("4점 책상"))
                .andExpect(jsonPath("$[1].content").value("4점 의자"))
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "miko",setupBefore = TestExecutionEvent.TEST_EXECUTION) //
    void deleteReview() throws Exception {
        Long reviewId = mikoDeskReview.getId();
        mvc.perform(delete("/api/reviews/{reviewId}", reviewId))
                .andExpect(status().isNoContent())
                .andDo(print());

        assertThat(reviewRepository.findById(reviewId)).isNotPresent();
    }
}
