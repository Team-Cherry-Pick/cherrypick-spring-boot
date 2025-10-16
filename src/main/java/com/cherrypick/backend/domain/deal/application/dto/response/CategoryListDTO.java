package com.cherrypick.backend.domain.deal.application.dto.response;

import com.cherrypick.backend.domain.deal.domain.entity.Category;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListDTO(List<CategoryDTO> categories) {

    @Builder
    public record CategoryDTO(
            Long categoryId,
            String name,
            List<CategoryDTO> subCategories
    ) {
        public static CategoryDTO of(Category category, List<CategoryDTO> subCategories) {
            return CategoryDTO.builder()
                    .categoryId(category.getCategoryId())
                    .name(category.getName())
                    .subCategories(subCategories)
                    .build();
        }

    }
}
