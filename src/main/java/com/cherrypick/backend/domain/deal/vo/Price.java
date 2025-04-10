package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.PriceType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Price {

    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    private double regularPrice;
    private double discountedPrice;
}
