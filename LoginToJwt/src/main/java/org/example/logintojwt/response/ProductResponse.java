package org.example.logintojwt.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.ProductImage;
import org.example.logintojwt.entity.Review;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private long price;
    private long quantity;
    private ProductCategoryResponse productCategoryResponse;
    private List<Review> reviewList;
    private List<ProductImage> productImageList;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .productCategoryResponse(ProductCategoryResponse.from(product.getCategory()))
                .reviewList(product.getReviewList())
                .productImageList(product.getProductImageList())
                .build();
    }
}
