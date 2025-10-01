package com.cherrypick.backend.domain.deal.domain.entity.vo;

import com.cherrypick.backend.domain.deal.domain.enums.ShippingType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

@Embeddable @Builder
public record ShippingVO(
        @Enumerated(EnumType.STRING)
        ShippingType shippingType,
        double shippingPrice,
        String shippingRule
) {}
