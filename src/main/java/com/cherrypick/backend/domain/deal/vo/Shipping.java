package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.ShippingType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Shipping {

    @Enumerated(EnumType.STRING)
    private ShippingType shippingType;

    private double shippingPrice;
    private String shippingRule;
}
