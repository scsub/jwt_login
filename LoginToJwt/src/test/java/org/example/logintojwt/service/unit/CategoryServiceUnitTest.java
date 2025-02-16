package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.Category;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.request.CategoryRequest;
import org.example.logintojwt.response.CategoryResponse;
import org.example.logintojwt.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {
    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Captor
    ArgumentCaptor<Category> categoryCaptor;


    private Category firMajorCategory;
    private Category secMajorCategory;
    private Category firMiddleCategory;
    private Category secMiddleCategory;
    private Category firSmallCategory;
    private Category secSmallCategory;

    private String fMajorName = "대분류1";
    private String sMajorName = "대분류2";
    private String fcMiddleName = "중분류1";
    private String scMiddleName = "중분류2";
    private String fcSmallName = "소분류1";
    private String scSmallName = "소분류2";

    @BeforeEach
    void setUp() {
        firMajorCategory = Category.builder()
                .name(fMajorName)
                .build();

        secMajorCategory = Category.builder()
                .name(sMajorName)
                .build();

        firMiddleCategory = Category.builder()
                .name(fcMiddleName)
                .parent(firMajorCategory)
                .build();

        secMiddleCategory = Category.builder()
                .name(scMiddleName)
                .parent(firMajorCategory)
                .build();

        firSmallCategory = Category.builder()
                .name(fcSmallName)
                .parent(firMiddleCategory)
                .build();

        secSmallCategory = Category.builder()
                .name(scSmallName)
                .parent(firMiddleCategory)
                .build();

        firMajorCategory.addChild(firMiddleCategory);
        firMajorCategory.addChild(secMiddleCategory);

        firMiddleCategory.addChild(firSmallCategory);
        firMiddleCategory.addChild(secSmallCategory);

    }

    @Test
    @DisplayName("대분류 카테고리 추가")
    void createCategoryWithNoParentTest() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0, Category.class);
            return new Category(1L, category.getName(), category.getParent(), category.getChildren(), category.getProducts());
        });

        categoryService.createCategory(fMajorName, null);

        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(fMajorName).isEqualTo(savedCategory.getName());
        assertThat(savedCategory.getParent()).isNull();
    }

    @Test
    @DisplayName("자식 카테고리 추가")
    void createCategoryWithParentTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(firMajorCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0, Category.class);
            return new Category(2L, category.getName(), category.getParent(), category.getChildren(), category.getProducts());
        });

        categoryService.createCategory(fcMiddleName, 1L);

        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(fcMiddleName).isEqualTo(savedCategory.getName());
        assertThat(firMajorCategory.getChildren()).contains(savedCategory);
    }

    @Test
    @DisplayName("최상위 카테고리 찾기")
    void findAllCategoriesTest() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(firMajorCategory, secMajorCategory));

        List<CategoryResponse> allCategories = categoryService.findAllCategories();

        verify(categoryRepository).findByParentIsNull();
        assertThat(allCategories).hasSize(2);
        assertThat(allCategories.get(0).getName()).isEqualTo(fMajorName);
        assertThat(allCategories.get(1).getName()).isEqualTo(sMajorName);
        assertThat(allCategories).isNotNull();
    }

    @Test
    @DisplayName("아이디로 카테고리 찾기")
    void findCategoryByIdTest() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.ofNullable(firMajorCategory));

        CategoryResponse categoryResponse = categoryService.findCategoryById(1L);

        verify(categoryRepository, times(1)).findById(1L);
        assertThat(categoryResponse.getName()).isEqualTo(fMajorName);
    }

    @Test
    @DisplayName("자식 카테고리 찾기")
    void findChildCategoryTest() {
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(firMiddleCategory, secMiddleCategory));

        List<CategoryResponse> categories = categoryService.findChildCategory(1L);

        assertThat(categories).hasSize(2);
        verify(categoryRepository, times(1)).findByParentId(1L);
        assertThat(categories.get(0).getName()).isEqualTo(fcMiddleName);
        assertThat(categories.get(1).getName()).isEqualTo(scMiddleName);
    }

    @Test
    @DisplayName("부모 카테고리 변경")
    void changeParentCategoryTest() {
        Category oldCategory = Category.builder()
                .name("old")
                .parent(null)
                .build();
        ReflectionTestUtils.setField(oldCategory, "id", 1L);

        Category childCategory = Category.builder()
                .name("child")
                .parent(oldCategory)
                .build();
        ReflectionTestUtils.setField(oldCategory, "id", 3L);

        Category newCategory = Category.builder()
                .name("new")
                .parent(null)
                .build();
        ReflectionTestUtils.setField(oldCategory, "id", 2L);

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(childCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        CategoryResponse response = categoryService.updateCategoryParent(2L, 3L);

        assertThat(childCategory.getParent()).isEqualTo(newCategory);
        assertThat(newCategory.getChildren()).contains(childCategory);
        assertThat(oldCategory.getChildren()).doesNotContain(childCategory);
    }

    @Test
    @DisplayName("카테고리 이름 변경")
    void updateCategoryNameTest() {
        Category category = Category.builder()
                .name("변경전")
                .parent(null)
                .build();
        setField(category, "id", 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.updateCategoryName("변경후", 1L);

        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(savedCategory.getName()).isEqualTo("변경후");
    }
}
