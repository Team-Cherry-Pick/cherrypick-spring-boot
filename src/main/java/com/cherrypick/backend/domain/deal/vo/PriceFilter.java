package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.PriceType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record PriceFilter (

        @Enumerated(EnumType.STRING)
        PriceType priceType,
        Double maxPrice,
        Double minPrice
){}
