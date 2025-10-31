package com.cherrypick.backend.global.log.domain.port;

import com.cherrypick.backend.global.log.domain.LogService;

import java.util.HashMap;

/**
 * 로그를 외부 시스템에 기록하는 추상화 인터페이스 (포트).
 * <p>
 * 헥사고날 아키텍처의 Port-Adapter 패턴을 적용하여,
 * 로깅 구현체(Logstash, Elasticsearch, File 등)를 교체 가능하도록 설계되었습니다.
 * </p>
 *
 * <h3>구현 예시:</h3>
 * <ul>
 *   <li>LogstashAdapter - ELK Stack으로 로그 전송</li>
 *   <li>FileLogAdapter - 파일 시스템에 로그 기록</li>
 *   <li>CloudWatchAdapter - AWS CloudWatch로 로그 전송</li>
 * </ul>
 *
 * @since 1.0
 * @see LogService
 */
public interface LogAppender
{
    /**
     * INFO 레벨의 구조화된 로그 메시지를 외부 시스템에 기록합니다.
     * <p>
     * 일반적인 애플리케이션 이벤트 로그에 사용됩니다.
     * (예: 액세스 로그, 로그인 로그, 구매 클릭 로그 등)
     * </p>
     *
     * @param logType 로그 타입 (예: "ACCESS_LOG", "LOGIN_LOG", "PURCHASE_CLICK_LOG")
     * @param message 로그 메시지 맵 (key-value 형태의 구조화된 데이터)
     * @throws IllegalArgumentException logType이 null이거나 비어있는 경우
     */
    void appendInfo(String logType, HashMap<String, Object> message);

    /**
     * ERROR 레벨의 구조화된 로그 메시지를 외부 시스템에 기록합니다.
     * <p>
     * 예외 및 오류 상황 로그에 사용됩니다.
     * (예: BaseException, RuntimeException, 스택 트레이스 포함)
     * </p>
     *
     * @param logType 로그 타입 (예: "ERROR_LOG")
     * @param message 로그 메시지 맵 (에러 메시지, 상태 코드, 스택 트레이스 포함)
     * @throws IllegalArgumentException logType이 null이거나 비어있는 경우
     */
    void appendError(String logType, HashMap<String, Object> message);
}
