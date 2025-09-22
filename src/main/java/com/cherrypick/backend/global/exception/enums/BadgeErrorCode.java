package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BadgeErrorCode implements BaseErrorCode {

    // 400 BAD REQUEST

    // 401 UNAUTHORIZED

    // 403 FORBIDDEN

    // 404 NOT_FOUND
    BADGE_NOT_FIND(HttpStatus.NOT_FOUND, "배지를 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    BadgeErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}