package org.example.logintojwt.service.integraion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.request.CategoryRequest;
import org.example.logintojwt.request.ProductRequest;
import org.example.logintojwt.service.CategoryService;
import org.example.logintojwt.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@Rollback
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 db로 자동 교체
@ExtendWith(SpringExtension.class)
public class CategoryAndProductIntegrationTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    // 통합
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    //
    private ProductRequest productRequest;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryRequest = CategoryRequest.builder()
                .name("대분류")
                .parentId(null)
                .build();

        productRequest = ProductRequest.builder()
                .name("목재")
                .price(10000L)
                .categoryId(1L)
                .description("나무로 만들어짐")
                .quantity(100L)
                .build();
    }

    @Test
    void createCategory() throws Exception {
        String json = objectMapper.writeValueAsString(categoryRequest);
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("대분류"))
                .andDo(category -> assertThat(categoryRepository.findByName("대분류")).isPresent());
    }

    @Test
    void createProductWithCategory() throws Exception {
        String json = objectMapper.writeValueAsString(categoryRequest);
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("대분류"))
                .andDo(category -> assertThat(categoryRepository.findByName("대분류")).isPresent());

        String productJson = objectMapper.writeValueAsString(productRequest);
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("목재"))
                .andExpect(jsonPath("$.productCategoryResponse.name").value("대분류"));
    }
}
