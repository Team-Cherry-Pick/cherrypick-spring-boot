package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VoteErrorCode implements BaseErrorCode {

    // ✅ 400 BAD REQUEST
    DISLIKE_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "비추천 시 사유를 작성해야 합니다."),
    DISLIKE_REASON_INVALID(HttpStatus.BAD_REQUEST, "추천 또는 무투표 상태에서는 비추천 사유를 작성할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    VoteErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}