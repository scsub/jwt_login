package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.CategoryRequest;
import org.example.logintojwt.response.CategoryResponse;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        List<CategoryResponse> parentCategory = categoryService.findAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(parentCategory);
    }

    @GetMapping("/{parentCategoryId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildrenCategories(@PathVariable Long parentCategoryId) {
        List<CategoryResponse> childCategory = categoryService.findChildCategory(parentCategoryId);
        return ResponseEntity.status(HttpStatus.OK).body(childCategory);
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        categoryService.createCategory(categoryRequest.getName(), categoryRequest.getParentId());
        SuccessResponse successResponse = new SuccessResponse("카테고리 생성");
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long categoryId) {
        CategoryResponse categoryResponse = categoryService.findCategoryById(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<SuccessResponse> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategoryById(categoryId);
        SuccessResponse successResponse = new SuccessResponse("카테고리 삭제 완료");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
