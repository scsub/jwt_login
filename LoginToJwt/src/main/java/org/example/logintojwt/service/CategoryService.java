package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Category;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.request.CategoryRequest;
import org.example.logintojwt.response.CategoryResponse;
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

    public void createCategory(String name, Long parentId) {
        Category category;
        if (parentId == null) { // 부모가 없음
            category = Category.builder().name(name).build();
        } else { // 부모가 있어서 부모를 카테고리 설정하고 부모를 설정
            Category parentCategory = categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 없습니다"));
            category = Category.builder().name(name).parent(parentCategory).build();
            parentCategory.addChild(category);
        }
        categoryRepository.save(category);
    }

    //전부다 찾기는 하나 1,2,3만 보여줘도 될걸 1,2,3 2,3, 3 이런식으로 보여줘서 사실상 사용하는게 아님
    public List<Category> findAllCategory() {
        return categoryRepository.findAll();
    }

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

    public CategoryResponse updateCategoryName(String updateName, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        category.updateCategoryName(updateName);
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    public CategoryResponse updateCategoryParent(Long parentId, Long id) {
        Category childCategory = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        Category newParentCategory = categoryRepository.findById(parentId).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        Category oldParentCategory = childCategory.getParent();
        if (oldParentCategory != null) {
            oldParentCategory.getChildren().remove(childCategory);
        }
        newParentCategory.addChild(childCategory);
        categoryRepository.save(childCategory);
        categoryRepository.save(newParentCategory);
        return CategoryResponse.from(childCategory);
    }

    // 삭제할때 자식 카테고리 까지 삭제된다
    //클라이언트에서 id를 받아올것
    public void deleteCategoryById(Long id) {
        categoryRepository.deleteById(id);
    }

    // 클라이언트에서 id주는 방법을 사용하지 못할경우 name을 받아 사용
    public void deleteCategoryByName(String name) {
        categoryRepository.deleteByName(name);
    }
}
