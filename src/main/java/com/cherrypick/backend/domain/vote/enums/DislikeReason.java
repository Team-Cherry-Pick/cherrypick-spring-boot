package com.cherrypick.backend.domain.vote.enums;

import lombok.Getter;

@Getter
public enum DislikeReason {

    // 상품 정보 틀림
    INCORRECT_PRODUCT_INFO,

    // 종료됨
    EXPIRED_DEAL,

    // 광고성 글
    ADVERTISEMENT,

    // 개인적 사유
    PERSONAL_DISLIKE
}
