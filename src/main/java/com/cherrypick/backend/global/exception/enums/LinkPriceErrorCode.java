package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum LinkPriceErrorCode implements BaseErrorCode {

    // ✅ 400 BAD REQUEST
    INVALID_ORIGINAL_URL(HttpStatus.BAD_REQUEST, "유효하지 않은 상품 링크입니다. 다시 확인해주시기 바랍니다."),
    // 전달받은 originalUrl이 null 또는 정책에 어긋날 경우

    // ✅ 502 BAD GATEWAY
    API_CLIENT_ERROR(HttpStatus.BAD_GATEWAY, "링크 정보를 요청하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주시기 바랍니다."),
    // 4xx 응답. 인증 키 누락, 파라미터 오류 등 클라이언트 측 요청 문제

    API_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "링크 생성 서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주시기 바랍니다."),
    // 5xx 응답. LinkPrice API 서버 내부 에러

    API_HOST_UNREACHABLE(HttpStatus.BAD_GATEWAY, "링크 생성 서버에 연결할 수 없습니다. 네트워크 상태를 확인해주시기 바랍니다."),
    // UnknownHostException. DNS 문제 또는 도메인 오류

    LINKPRICE_API_RESULT_FAIL(HttpStatus.BAD_GATEWAY, "링크 생성에 실패했습니다. 다른 상품으로 다시 시도해주시기 바랍니다."),
    // 응답 JSON의 `"result"` 값이 `"S"`가 아닐 경우

    INVALID_API_RESPONSE_FORMAT(HttpStatus.BAD_GATEWAY, "링크 생성 결과 처리 중 오류가 발생했습니다."),
    // JSON 파싱 실패. 응답 형식이 예외적이거나 깨짐

    LINKPRICE_API_EXCEPTION(HttpStatus.BAD_GATEWAY, "링크 생성 중 알 수 없는 오류가 발생했습니다."),
    // 기타 예외 (IOException, 예기치 못한 에러 등)

    // ✅ 504 GATEWAY TIMEOUT
    API_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "링크 생성 요청 시간이 초과되었습니다. 잠시 후 다시 시도해주시기 바랍니다.");

    private final HttpStatus status;
    private final String message;

    LinkPriceErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
