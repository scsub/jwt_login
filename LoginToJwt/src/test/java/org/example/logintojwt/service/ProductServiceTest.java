package org.example.logintojwt.service;

import org.example.logintojwt.entity.Category;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    void searchProduct() {
        Category category = Category.builder()
                .parent(null)
                .name("카테고리")
                .build();

        Product firstProduct = Product.builder()
                .name("첫번째 아이템")
                .description("처음으로 파는것")
                .price(1000L)
                .quantity(100L)
                .category(category)
                .build(); // 카테고리는 이 테스트에는 필요없음

        Product secondProduct = Product.builder()
                .name("두번째 아이템")
                .description("두번쨰로 파는것")
                .price(2000L)
                .quantity(200L)
                .category(category)
                .build();

        when(productRepository.findByNameContaining("첫번째")).thenReturn(List.of(firstProduct));
        when(productRepository.findByNameContaining("두번째")).thenReturn(List.of(secondProduct));
        when(productRepository.findByNameContaining("아이템")).thenReturn(List.of(firstProduct, secondProduct));

        List<ProductResponse> firRes = productService.searchProductByName("첫번째");
        List<ProductResponse> secRes = productService.searchProductByName("두번째");
        List<ProductResponse> bothRes = productService.searchProductByName("아이템");


        verify(productRepository, times(1)).findByNameContaining("첫번째");
        verify(productRepository, times(1)).findByNameContaining("두번째");
        verify(productRepository, times(1)).findByNameContaining("아이템");
        assertThat(firRes).hasSize(1);
        assertThat(bothRes).hasSize(2);
        assertThat(bothRes.get(0).getPrice()).isEqualTo(firstProduct.getPrice());
        assertThat(bothRes.get(1).getPrice()).isEqualTo(secondProduct.getPrice());
    }
}