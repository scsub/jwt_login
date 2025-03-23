package org.example.logintojwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.logintojwt.request.ReviewRequest;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long rating;

    private String content;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public static Review from(ReviewRequest reviewRequest, User user, Product product) {
        return Review.builder()
                .rating(reviewRequest.getRating())
                .content(reviewRequest.getContent())
                .createdAt(LocalDateTime.now())
                .user(user)
                .product(product)
                .build();
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public void updateProduct(Product product) {
        this.product = product;
    }
}
