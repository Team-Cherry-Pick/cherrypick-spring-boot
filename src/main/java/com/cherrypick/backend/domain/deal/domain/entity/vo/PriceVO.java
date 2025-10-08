package com.cherrypick.backend.domain.deal.domain.entity.vo;

import com.cherrypick.backend.domain.deal.domain.enums.PriceType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TODO : 파일명 수정요함
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PriceVO {
    @Enumerated(EnumType.STRING)
    private PriceType priceType;
    private double regularPrice;
    private double discountedPrice;
}

