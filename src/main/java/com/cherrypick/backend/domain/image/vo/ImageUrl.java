package com.cherrypick.backend.domain.image.vo;

import jakarta.persistence.Embeddable;
import lombok.Builder;

@Embeddable @Builder
public record ImageUrl(
        Long imageId,
        String url,
        int indexes
) {}
