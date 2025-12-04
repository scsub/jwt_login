package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Category;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.response.CategoryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @PreAuthorize("hasRole('USER')")
    public void createCategory(String categoryName, Long parentId) {
        Category category;
        if (parentId == null) { // 부모가 없음
            category = Category.builder().name(categoryName).build();
        } else { // 부모가 있어서 부모를 카테고리 설정하고 부모를 설정
            Category parentCategory = categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다"));
            category = Category.builder().name(categoryName).parent(parentCategory).build();
            parentCategory.addChild(category);
        }
        categoryRepository.save(category);
    }

    //전부다 찾기는 하나 1,2,3만 보여줘도 될걸 1,2,3 2,3, 3 이런식으로 보여줘서 사실상 사용하는게 아님
    //public List<Category> findAllCategory() {
    //    return categoryRepository.findAll();
    //}


    public List<CategoryResponse> findAllCategories() {
        List<Category> categories = categoryRepository.findByParentIsNull();
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }


    public List<CategoryResponse> findChildCategory(Long parentId) {
        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categories.stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse findCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        return CategoryResponse.from(category);
    }

    public CategoryResponse findCategoryByName(String name) {
        Category category = categoryRepository.findByName(name).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        return CategoryResponse.from(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategoryName(String updateName, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        category.updateCategoryName(updateName);
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateParentCategory(Long newParentId, Long id) {
        Category childCategory = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        Category newParentCategory = categoryRepository.findById(newParentId).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        Category oldParentCategory = childCategory.getParent();
        if (oldParentCategory != null) {
            oldParentCategory.getChildren().remove(childCategory);
            categoryRepository.save(oldParentCategory);
        }
        newParentCategory.addChild(childCategory);
        categoryRepository.save(childCategory);
        categoryRepository.save(newParentCategory);
        return CategoryResponse.from(childCategory);
    }

    // 삭제할때 자식 카테고리 까지 삭제된다
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategoryById(Long id) {
        categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        categoryRepository.deleteById(id);
    }
}
