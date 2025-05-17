package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.ProductImage;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageResponse {
    private Long id;
    private String url;

    public static ProductImageResponse from(ProductImage productImage) {
        return ProductImageResponse.builder()
                .id(productImage.getId())
                .url(productImage.getUrl())
                .build();
    }

    public static List<ProductImageResponse> fromList(List<ProductImage> productImages) {
        return productImages.stream()
                .map(productImage -> ProductImageResponse.from(productImage))
                .collect(Collectors.toList());
    }
}
