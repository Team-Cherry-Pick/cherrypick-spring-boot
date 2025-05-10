package com.cherrypick.backend.domain.category.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListDTO(List<CategoryDTO> categories) {

    @Builder
    public record CategoryDTO(
            Long id,
            String name,
            List<CategoryDTO> subCategories
    ) {}
}
