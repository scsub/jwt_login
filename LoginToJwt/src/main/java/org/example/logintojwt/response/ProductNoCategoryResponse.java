package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Product;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductNoCategoryResponse {
    private Long id;
    private String name;
    private String description;
    private long price;
    private long quantity;

    public static ProductNoCategoryResponse from(Product product) {
        return ProductNoCategoryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }
}
