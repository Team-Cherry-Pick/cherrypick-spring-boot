package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DealErrorCode implements BaseErrorCode {

    // ✅ 400 BAD REQUEST
    INVALID_DEAL_REQUEST(HttpStatus.BAD_REQUEST, "핫딜 요청이 올바르지 않습니다."),
    MISSING_REQUIRED_FIELDS(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다."),
    INVALID_PRICE_INFORMATION(HttpStatus.BAD_REQUEST, "가격 정보가 유효하지 않습니다."),
    INVALID_DISCOUNT_INFORMATION(HttpStatus.BAD_REQUEST, "할인 정보가 유효하지 않습니다."),
    INVALID_PRICE_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 가격 타입입니다."),
    INVALID_SHIPPING_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 배송 타입입니다."),

    // ✅ 404 NOT FOUND
    DEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 핫딜 게시글입니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스토어입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    DISCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 할인 정보입니다.");

    private final HttpStatus status;
    private final String message;

    DealErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
