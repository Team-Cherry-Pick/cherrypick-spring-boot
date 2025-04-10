package com.cherrypick.backend.domain.user.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum UserStatus {
    ACTIVE,         // 활성
    DORMANT,        // 휴면
    DEACTIVATED     // 비활성(삭제 이전 단계)
}
