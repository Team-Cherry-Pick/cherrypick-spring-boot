package com.cherrypick.backend.domain.category.repository;

import com.cherrypick.backend.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);  // 카테고리 ID로 카테고리 조회
}
