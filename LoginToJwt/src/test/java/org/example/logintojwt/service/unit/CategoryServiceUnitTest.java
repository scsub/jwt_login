package org.example.logintojwt.service.unit;

import org.example.logintojwt.entity.Category;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.response.CategoryResponse;
import org.example.logintojwt.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {
    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Captor
    ArgumentCaptor<Category> categoryCaptor;

    private Category major;
    private Category middle;
    private Category small;
    private Category newMiddle;
    private Category newSmall;
    private Category newMajor;
    private String majorName = "대분류";
    private String middleName = "중분류";
    private String smallName = "소분류";
    private String newMiddleName = "새로운 중분류";
    private String newSmallName = "새로운 소분류";
    private String newMajorName = "새로운 대분류";

    @BeforeEach
    void setUp() {
        major = Category.builder()
                .id(1L)
                .name(majorName)
                .parent(null)
                .build();

        middle = Category.builder()
                .id(2L)
                .name(middleName)
                .parent(major)
                .build();

        small = Category.builder()
                .id(3L)
                .name(smallName)
                .parent(middle)
                .build();
        major.addChild(middle);
        middle.addChild(small);
        newMajor = Category.builder()
                .id(20L)
                .name(newMajorName)
                .parent(null)
                .build();


    }

    @Test
    @DisplayName("대분류 카테고리 생성")
    void createCategoryWithNoParentTest() {
        // 실행
        categoryService.createCategory(majorName, null);
        // 검증
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(savedCategory.getName()).isEqualTo(majorName);
        assertThat(savedCategory.getParent()).isNull();
    }

    @Test
    @DisplayName("대분류에 중분류,중분류에 소분류 카테고리 추가")
    void createCategoryWithParent() {
        String newMiddleCategoryName = "새로운 중분류 카테고리";
        String newSmallCategoryName = "새로운 소분류 카테고리";

        when(categoryRepository.findById(major.getId())).thenReturn(Optional.of(major));
        when(categoryRepository.findById(middle.getId())).thenReturn(Optional.of(middle));

        categoryService.createCategory(newMiddleCategoryName, major.getId());
        categoryService.createCategory(newSmallCategoryName, middle.getId());

        verify(categoryRepository, times(1)).findById(major.getId());
        verify(categoryRepository, times(1)).findById(middle.getId());

        verify(categoryRepository, times(2)).save(categoryCaptor.capture());
        Category savedMiddle = categoryCaptor.getAllValues().get(0);
        Category savedSmall = categoryCaptor.getAllValues().get(1);
        assertThat(savedMiddle.getName()).isEqualTo(newMiddleCategoryName);
        assertThat(savedSmall.getName()).isEqualTo(newSmallCategoryName);
        assertThat(savedMiddle.getParent()).isEqualTo(major);
        assertThat(savedSmall.getParent()).isEqualTo(middle);
    }

    @Test
    @DisplayName("부모가 없어서 카테고리 추가 못함")
    void createCategoryWithParent_no_parent() {
        Long notParentId = 999L;
        String newMiddleCategoryName = "새로운 중분류 카테고리";
        when(categoryRepository.findById(notParentId)).thenReturn(Optional.empty());
        //
        assertThatThrownBy(() -> categoryService.createCategory(newMiddleCategoryName, notParentId))
                .isInstanceOf(IllegalArgumentException.class);
        //
        verify(categoryRepository).findById(notParentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("전체 카테고리 가져오기")
    void find_all_category() {
        Category secMajor = Category.builder()
                .id(10L)
                .parent(null)
                .name("두번째 대분류")
                .build();
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(major, secMajor));
        //
        List<CategoryResponse> allCategories = categoryService.findAllCategories();
        //
        verify(categoryRepository, times(1)).findByParentIsNull();
        assertThat(allCategories).hasSize(2);
        assertThat(allCategories.get(0).getName()).isEqualTo(majorName);
        assertThat(allCategories.get(0).getChildren().get(0).getName()).isSameAs(middleName);
        assertThat(allCategories.get(1).getName()).isEqualTo(secMajor.getName());
    }

    @Test
    @DisplayName("부모 카테고리 id로 자식 카테고리 가져오기")
    void find_child_category() {
        when(categoryRepository.findByParentId(major.getId())).thenReturn(List.of(middle));
        //
        List<CategoryResponse> childCategory = categoryService.findChildCategory(major.getId());
        //
        verify(categoryRepository, times(1)).findByParentId(major.getId());
        assertThat(childCategory).hasSize(1);
        assertThat(childCategory.get(0).getName()).isEqualTo(middleName);
    }

    @Test
    @DisplayName("카테고리 id로 카테고리 하나만 가져오기")
    void find_one_category_by_id() {
        when(categoryRepository.findById(major.getId())).thenReturn(Optional.of(major));
        //
        CategoryResponse category = categoryService.findCategoryById(major.getId());
        //
        verify(categoryRepository, times(1)).findById(major.getId());
        assertThat(category.getName()).isEqualTo(majorName);
    }

    @Test
    @DisplayName("카테고리 id로 카테고리를 찾지 못함")
    void not_find_one_category_by_id() {
        when(categoryRepository.findById(major.getId())).thenReturn(Optional.empty());
        //
        assertThatThrownBy(() -> categoryService.findCategoryById(major.getId())).isInstanceOf(IllegalArgumentException.class);
        //
        verify(categoryRepository, times(1)).findById(major.getId());
    }

    @Test
    @DisplayName("카테고리 이름으로 카테고리 찾기")
    void find_one_category_by_name() {
        when(categoryRepository.findByName(major.getName())).thenReturn(Optional.of(major));
        //
        CategoryResponse categoryByName = categoryService.findCategoryByName(majorName);
        //
        verify(categoryRepository, times(1)).findByName(major.getName());
        assertThat(categoryByName.getName()).isEqualTo(majorName);
    }

    @Test
    @DisplayName("카테고리 이름으로 카테고리 찾지 못함")
    void not_find_one_category_by_name() {
        when(categoryRepository.findByName(major.getName())).thenReturn(Optional.empty());
        //
        assertThatThrownBy(() -> categoryService.findCategoryByName(major.getName())).isInstanceOf(IllegalArgumentException.class);
        //
        verify(categoryRepository, times(1)).findByName(major.getName());
    }

    @Test
    @DisplayName("카테고리 이름 변경")
    void change_category_name() {
        String newCategoryName = "newMajor";
        when(categoryRepository.findById(major.getId())).thenReturn(Optional.of(major));
        //
        CategoryResponse majorResponse = categoryService.updateCategoryName(newCategoryName, major.getId());
        //
        verify(categoryRepository, times(1)).findById(major.getId());
        verify(categoryRepository, times(1)).save(categoryCaptor.capture());
        Category savedMajor = categoryCaptor.getValue();
        assertThat(savedMajor.getName()).isEqualTo(newCategoryName);
        assertThat(majorResponse.getName()).isEqualTo(newCategoryName);
    }

    @Test
    @DisplayName("카테고리 이름 변경 실패 카테고리를 찾지 못함")
    void change_category_name_not_found_id() {
        when(categoryRepository.findById(major.getId())).thenReturn(Optional.empty());
        //
        assertThatThrownBy(() -> categoryService.updateCategoryName("newMajor", major.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        //
        verify(categoryRepository, times(1)).findById(major.getId());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리의 부모 카테고리 변경 / 중분류가 대분류를 바꾸려는 상황")
    void change_parent_category() {
        when(categoryRepository.findById(middle.getId())).thenReturn(Optional.of(middle));
        when(categoryRepository.findById(newMajor.getId())).thenReturn(Optional.of(newMajor));
        //
        CategoryResponse categoryResponse = categoryService.updateParentCategory(newMajor.getId(), middle.getId());
        //
        verify(categoryRepository, times(3)).save(categoryCaptor.capture());
        List<Category> allValues = categoryCaptor.getAllValues();
        assertThat(allValues).hasSize(3);
        Category oldParentCategory = allValues.get(0);
        Category childCategory = allValues.get(1);
        Category newParentCategory = allValues.get(2);
        assertThat(childCategory.getName()).isEqualTo(middle.getName());
        assertThat(newParentCategory.getName()).isEqualTo(newMajor.getName());
    }

    @Test
    @DisplayName("id로 카테고리 삭제")
    void delete_category_by_id() {
        when(categoryRepository.findById(major.getId())).thenReturn(Optional.of(major));
        //
        categoryService.deleteCategoryById(major.getId());
        //
        verify(categoryRepository, times(1)).findById(major.getId());
        verify(categoryRepository, times(1)).deleteById(major.getId());
    }
}
