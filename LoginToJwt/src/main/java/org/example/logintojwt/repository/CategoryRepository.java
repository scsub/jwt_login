package org.example.logintojwt.repository;

import org.example.logintojwt.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentId(Long parentId);
    List<Category> findByParentIsNull();
    Optional<Category> findByName(String name);
    void deleteByName(String name);
}
