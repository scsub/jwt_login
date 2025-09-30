package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Review;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponse {
    private Boolean recommend;
    private String content;
    private LocalDateTime createdAt;
    private String username;

    public static ReviewResponse from(Review review){
        return ReviewResponse.builder()
                .recommend(review.getRecommend())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .username(review.getUser().getUsername())
                .build();
    }

}
