package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.Review;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.ReviewRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.ReviewRequest;
import org.example.logintojwt.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceUnitTest {
    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @BeforeEach
    void setUp() {

    }

    @Test
    void createReview() {
        ReviewRequest reviewRequest = ReviewRequest.builder()
                .userId(1L)
                .productId(1L)
                .rating(5L)
                .content("상품 평가")
                .build();

        User user = User.builder()
                .username("첫번째 유저")
                .build();

        Product product = Product.builder()
                .name("가구")
                .build();

        Review review = Review.from(reviewRequest, user, product);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // 커스텀 디테일 추가
        //reviewService.createReview(reviewRequest);

        verify(userRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(reviewRepository, times(1)).save(any(Review.class));

        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getProduct()).isEqualTo(product);
    }

    @Test
    void getReviewByUseridAndProductId() {
        User user = User.builder()
                .username("첫번째 유저")
                .build();

        Product product = Product.builder()
                .name("가구")
                .build();

        Review review1 = Review.builder()
                .id(1L)
                .user(user)
                .product(product)
                .rating(1L)
                .content("나빠요")
                .build();

        Review review2 = Review.builder()
                .id(2L)
                .user(user)
                .product(product)
                .rating(5L)
                .content("좋아요")
                .build();

        List<Review> reviewList = List.of(review1, review2);

        when(reviewRepository.findByUserId(1L)).thenReturn(reviewList);

        reviewService.findAllReviewByUserId(1L);
        reviewService.findAllReviewByProductId(5L);

        verify(reviewRepository, times(1)).findByUserId(1L);
        verify(reviewRepository, times(1)).findByProductId(5L);
    }

    @Test
    void deleteReview() {
        Long reviewId = 1L;
        Long userId = 5L;
        User user = User.builder()
                .username("첫번째")
                .build();

        Review review = Review.builder()
                .id(reviewId)
                .user(user)
                .build();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.deleteReview(reviewId, userId);

        verify(reviewRepository,times(1)).findById(reviewId);
        verify(reviewRepository, times(1)).deleteById(reviewId);

    }
}