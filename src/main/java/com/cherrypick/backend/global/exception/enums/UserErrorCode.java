package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements BaseErrorCode {

    // 400 BAD REQUEST
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다."),
    REFRESH_TOKEN_NOT_VALID(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "리프레시 토큰이 만료되었습니다."),

    // 401 UNAUTHORIZED
    SECURITY_AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // 403 FORBIDDEN
    ADMIN_AUTHENTICATION_REQUIRED(HttpStatus.FORBIDDEN, "관리자 인증이 필요합니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰이 없습니다."),
    UNDEFINED_OAUTH_PROVIDER(HttpStatus.NOT_FOUND, "잘못된 소셜 로그인 제공자입니다."),

    ;

    private final HttpStatus status;
    private final String message;

    UserErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}