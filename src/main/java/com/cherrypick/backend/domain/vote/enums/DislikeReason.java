package com.cherrypick.backend.domain.vote.enums;

import lombok.Getter;

@Getter
public enum DislikeReason {

    // 가격이 아쉬워요
    BAD_PRICE,

    // 품질이 아쉬워요
    BAD_QUALITY,

    // 상품 정보가 틀렸어요
    WRONG_INFO,

    // 품절 상품이에요
    SOLD_OUT,

    // 광고성 글 같아요
    ADVERTISING,

    // 기타
    OTHER
}
