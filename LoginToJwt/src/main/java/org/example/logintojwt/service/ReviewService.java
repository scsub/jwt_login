package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetails;
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
import org.springframework.security.core.userdetails.UserDetails;
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

    public ReviewRequest createReview(ReviewRequest reviewRequest, CustomUserDetails userDetails) {
        Long userDetailsId = userDetails.getId();
        if (userDetailsId!=reviewRequest.getUserId()) {
            throw new UserNotFoundException("로그인한 유저ID와 제출 ID가 맞지않음");
        }
        User user = userRepository.findById(reviewRequest.getUserId()).orElseThrow(() -> new UserNotFoundException("ID로 유저를 찾을수없음"));
        Product product = productRepository.findById(reviewRequest.getProductId()).orElseThrow(() -> new ProductNotFoundException("ID로 상품을 찾을수없음"));
        Review review = Review.from(reviewRequest, user, product);

        product.addReview(review);
        user.addReview(review);
        Review savedReview = reviewRepository.save(review);
        return ReviewRequest.from(savedReview);
    }

    public List<ReviewRequest>  findAllReviewByUserId(Long userId) {
        List<Review> reviewList = reviewRepository.findByUserId(userId);
        List<ReviewRequest> reviewRequestList = reviewList.stream()
                .map(review -> ReviewRequest.from(review))
                .collect(Collectors.toList());

        return reviewRequestList;
    }

    public List<ReviewRequest> findAllReviewByProductId(Long productId) {
        List<Review> reviewList = reviewRepository.findByProductId(productId);
        List<ReviewRequest> reviewRequestList = reviewList.stream()
                .map(review -> ReviewRequest.from(review))
                .collect(Collectors.toList());
        return reviewRequestList;
    }

    public void deleteReview(Long reviewId,Long userId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ReviewNotFoundException("ID로 리뷰를 찾을수없음"));
        if (review.getUser().getId().equals(userId)) {
            reviewRepository.deleteById(reviewId);
        } else {
            throw new SecurityException("리뷰 삭제 권한이 없습니다");
        }
    }
}
