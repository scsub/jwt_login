package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private List<CategoryResponse> children = new ArrayList<>();
    private List<ProductNoCategoryResponse> products = new ArrayList<>();

    public static CategoryResponse from(Category category) {
        List<CategoryResponse> children = category.getChildren() != null ?
                category.getChildren().stream()
                        .map(child -> from(child))
                        .collect(Collectors.toList()) : null;

        List<ProductNoCategoryResponse> productsResponse = category.getProducts() != null ?
                category.getProducts().stream()
                        .map(product -> ProductNoCategoryResponse.from(product))
                        .collect(Collectors.toList()) : null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .children(children)
                .products(productsResponse)
                .build();
    }

}
