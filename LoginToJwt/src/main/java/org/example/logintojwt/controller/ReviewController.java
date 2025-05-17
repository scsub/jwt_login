package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.ReviewRequest;
import org.example.logintojwt.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest reviewRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId();
        ReviewRequest request = reviewService.createReview(reviewRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> findAllReviewsByProductId(@PathVariable Long productId) {
        List<ReviewRequest> allReviewByProductId = reviewService.findAllReviewByProductId(productId);
        return ResponseEntity.status(HttpStatus.OK).body(allReviewByProductId);
    }

    @GetMapping("/user")
    public ResponseEntity<?> findAllReviewsByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<ReviewRequest> allReviewByUserId = reviewService.findAllReviewByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allReviewByUserId);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
