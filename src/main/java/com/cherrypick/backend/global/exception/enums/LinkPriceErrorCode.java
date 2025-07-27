package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum LinkPriceErrorCode implements BaseErrorCode {

    // ✅ 400 BAD REQUEST
    INVALID_ORIGINAL_URL(HttpStatus.BAD_REQUEST, "유효하지 않은 원본 URL입니다."),

    // ✅ 502 BAD GATEWAY
    API_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "LinkPrice API 클라이언트 오류"),
    API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "LinkPrice API 서버 오류"),
    API_HOST_UNREACHABLE(HttpStatus.BAD_GATEWAY, "LinkPrice API 서버 연결 불가"),
    LINKPRICE_API_RESULT_FAIL(HttpStatus.BAD_GATEWAY, "LinkPrice API 변환 실패 (result != S)"),
    INVALID_API_RESPONSE_FORMAT(HttpStatus.BAD_GATEWAY, "LinkPrice API 응답 포맷 오류"),
    LINKPRICE_API_EXCEPTION(HttpStatus.BAD_GATEWAY, "LinkPrice API 처리 중 알 수 없는 예외"),

    // ✅ 504 GATEWAY TIMEOUT
    API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "LinkPrice API 응답 시간 초과");

    private final HttpStatus status;
    private final String message;

    LinkPriceErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
