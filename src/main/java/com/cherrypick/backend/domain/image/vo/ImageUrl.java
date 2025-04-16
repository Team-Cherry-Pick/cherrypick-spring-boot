package com.cherrypick.backend.domain.image.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public record ImageUrl(
        Long imageId,
        String url,
        int index
) {}
