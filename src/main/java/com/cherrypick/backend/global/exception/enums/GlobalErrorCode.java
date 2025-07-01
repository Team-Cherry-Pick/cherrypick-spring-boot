package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements BaseErrorCode {

    // 존재하지 않는 유저입니다, 존재하지 않는 게시글입니다 등등 자신이 맡은 도메인에 맞게 ENUM 생성해서 사용하면 됨
    // 예시로 UserErrorCode 만들어놨고, 꼭!! BaseErrorCode 상속 받아야함 !!!
    // BaseException 에서는 500에러는 다루지 않을 거니까 ENUM에 넣지 않기 !

    // ✅ 400 BAD REQUEST
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    NO_COOKIES_TO_READ(HttpStatus.BAD_REQUEST, "읽을 수 있는 쿠키가 존재하지 않습니다."),

    // ✅ 401 UNAUTHORIZED (인증 관련 오류)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 제공되지 않았습니다."),
    TOKEN_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "토큰 검증에 실패했습니다."),

    // ✅ 403 FORBIDDEN (권한 관련 오류)
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "토큰이 일치하지 않습니다."),

    // ✅ 404 NOT FOUND
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    GlobalErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
