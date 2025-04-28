package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.ShippingType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

@Embeddable @Builder
public record Shipping(
        @Enumerated(EnumType.STRING)
        ShippingType shippingType,
        double shippingPrice,
        String shippingRule
) {}
