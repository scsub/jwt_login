package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.config.security.CustomUserDetails;
import org.example.logintojwt.request.ReviewRequest;
import org.example.logintojwt.response.ReviewResponse;
import org.example.logintojwt.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Void> createReview(@RequestBody ReviewRequest reviewRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getId();
        log.debug(reviewRequest.toString());
        reviewService.createReview(reviewRequest, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ReviewResponse>> findAllReviewsByProductId(@PathVariable Long productId) {
        List<ReviewResponse> allReviewByProductId = reviewService.findAllReviewByProductId(productId);
        return ResponseEntity.status(HttpStatus.OK).body(allReviewByProductId);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReviewResponse>> findAllReviewsByUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<ReviewResponse> allReviewByUserId = reviewService.findAllReviewByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(allReviewByUserId);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
