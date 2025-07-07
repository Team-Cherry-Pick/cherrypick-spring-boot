package com.cherrypick.backend.global.exception.handler;

import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.dto.response.ErrorResponseDTO;
import com.cherrypick.backend.global.util.LogService;
import com.cherrypick.backend.global.util.SlackNotifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

import static org.springframework.http.ResponseEntity.status;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final SlackNotifier slackNotifier;
    private final LogService logService;

    // BaseException 핸들링 (ErrorCode 발생)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleBaseException(BaseException ex, HttpServletRequest request) {
        String fullPath = request.getMethod() + " " + request.getRequestURI();

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(ex.getErrorCode(), fullPath);
        logService.errorLog(ex.getErrorCode().getStatus(), ex.getMessage(), ex.getStackTrace());
        return status(ex.getErrorCode().getStatus()).body(errorResponse);
    }

    // 예상치 못한 RuntimeException 핸들링 (500 에러)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String fullPath = request.getMethod() + " " + request.getRequestURI(); // HTTP 메소드 포함한 경로

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                500,
                "Internal Server Error",
                ex.getMessage(),
                fullPath,
                LocalDateTime.now()
        );

        // 8080 포트에서 실행 중일 때 슬랙 알림을 보내지 않음
        // TODO : Sprig Profile로 변경함으로써 일관성 획득.
        int port = request.getServerPort();
        if (port != 8080) {
            slackNotifier.sendErrorLog(ex, request);  // 슬랙 알림 전송
        }
        logService.errorLog(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getStackTrace());

        return ResponseEntity.status(500).body(errorResponse);
    }
}