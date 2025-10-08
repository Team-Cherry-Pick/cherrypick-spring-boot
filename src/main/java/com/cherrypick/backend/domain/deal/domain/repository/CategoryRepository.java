package com.cherrypick.backend.domain.deal.domain.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 카테고리 ID로 카테고리 조회
    Optional<Category> findById(Long id);

    // 1레벨 카테고리 조회
    Optional<Category> findByCategoryIdAndParentIdIsNull(Long categoryId);

}
