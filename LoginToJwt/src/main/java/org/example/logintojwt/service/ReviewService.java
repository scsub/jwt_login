package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.Review;
import org.example.logintojwt.entity.User;
import org.example.logintojwt.exception.ProductNotFoundException;
import org.example.logintojwt.exception.ReviewNotFoundException;
import org.example.logintojwt.exception.UserNotFoundException;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.repository.ReviewRepository;
import org.example.logintojwt.repository.UserRepository;
import org.example.logintojwt.request.ReviewRequest;
import org.example.logintojwt.response.ReviewResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public void createReview(ReviewRequest reviewRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("id","ID로 유저를 찾을수없음"));
        Product product = productRepository.findById(reviewRequest.getProductId()).orElseThrow(() -> new ProductNotFoundException("id","ID로 상품을 찾을수없음"));
        Review review = Review.from(reviewRequest, user, product);

        product.addReview(review);
        user.addReview(review);
        Review savedReview = reviewRepository.save(review);
    }

    public List<ReviewResponse>  findAllReviewByUserId(Long userId) {
        List<Review> reviewList = reviewRepository.findByUserId(userId);
        List<ReviewResponse> reviewResponseList = reviewList.stream()
                .map(review -> ReviewResponse.from(review))
                .collect(Collectors.toList());

        return reviewResponseList;
    }

    public List<ReviewResponse> findAllReviewByProductId(Long productId) {
        List<Review> reviewList = reviewRepository.findByProductId(productId);
        List<ReviewResponse> reviewResponseList = reviewList.stream()
                .map(review -> ReviewResponse.from(review))
                .collect(Collectors.toList());
        return reviewResponseList;
    }

    public void deleteReview(Long reviewId,Long userId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException("id","리뷰를 찾을수없음"));
        if (review.getUser().getId().equals(userId)) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new SecurityException("리뷰 삭제 권한이 없습니다");
        }
    }
}
