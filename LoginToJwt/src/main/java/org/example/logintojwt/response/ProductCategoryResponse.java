package org.example.logintojwt.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.logintojwt.entity.Category;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    // 자식은 굳이 필요없음 상품 클릭할때 해당 카테고리나 부모만 궁금하지 자식은 궁금하지않음

    public static ProductCategoryResponse from(Category category) {
        return ProductCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
