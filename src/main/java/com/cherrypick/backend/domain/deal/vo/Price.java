package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.PriceType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

// TODO : 파일명 수정요함
@Embeddable @Builder
public record Price(
        @Enumerated(EnumType.STRING)
        PriceType priceType,
        double regularPrice,
        double discountedPrice
) {}

