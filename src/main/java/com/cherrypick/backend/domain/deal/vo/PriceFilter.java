package com.cherrypick.backend.domain.deal.vo;

import com.cherrypick.backend.domain.deal.enums.PriceType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

// TODO : 파일명 수정요함
@Embeddable
public record PriceFilter (

        @Enumerated(EnumType.STRING)
        PriceType priceType,
        Double maxPrice,
        Double minPrice
){}
