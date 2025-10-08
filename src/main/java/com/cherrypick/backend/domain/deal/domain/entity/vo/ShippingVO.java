package com.cherrypick.backend.domain.deal.domain.entity.vo;

import com.cherrypick.backend.domain.deal.domain.enums.ShippingType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ShippingVO {
    @Enumerated(EnumType.STRING)
    private ShippingType shippingType;
    private double shippingPrice;
    private String shippingRule;
}
