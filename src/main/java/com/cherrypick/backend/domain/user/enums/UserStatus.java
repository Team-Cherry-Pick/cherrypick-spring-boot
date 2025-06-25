package com.cherrypick.backend.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE,         // 활성
    PENDING,        // 가입은 했으나 정보 미입력 단계
    DORMANT,        // 휴면
    DEACTIVATED     // 비활성(삭제 이전 단계)
}
