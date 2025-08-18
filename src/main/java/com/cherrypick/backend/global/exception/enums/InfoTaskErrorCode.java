package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum InfoTaskErrorCode implements BaseErrorCode {


    // ✅ 400 BAD REQUEST

    // ✅ 401 UNAUTHORIZED

    // ✅ 403 FOBIDDEN

    // ✅ 404 NOT FOUND

    // ✅ 503 Service Unavailable
    TASK_QUEUE_OVERLOADED(HttpStatus.SERVICE_UNAVAILABLE, "작업 큐가 포화상태여서 정보를 불러올 수 없습니다."),
    TASK_RESPONSE_TIMEOUT (HttpStatus.SERVICE_UNAVAILABLE, "작업 대기 시간이 초과되었습니다."),
    SYSTEM_INTERRUPTED(HttpStatus.SERVICE_UNAVAILABLE, "작업이 시스템에 의해 중단되었습니다."),
    TASK_EXECUTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "작업 처리 중 오류가 발생했습니다.")
    ;

    private final HttpStatus status;
    private final String message;

    InfoTaskErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}