package com.cherrypick.backend.global.exception.dto.response;

import com.cherrypick.backend.global.exception.BaseErrorCode;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,             // HTTP 상태 코드 (400, 404 등)
        String error,           // HTTP 상태에 대한 설명 (Bad Request)
        String message,         // 해당 에러에 대한 상세 메시지 (잘못된 요청입니다.)
        String path,            // 에러가 발생한 요청 EndPoint (GET /api/test)
        LocalDateTime timestamp // 에러 발생 시간
) {
    public ErrorResponseDTO(BaseErrorCode errorCode, String path) {
        this(
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.getMessage(),
                path,
                LocalDateTime.now()
        );
    }
}