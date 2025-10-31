package com.cherrypick.backend.global.exception;

import com.cherrypick.backend.global.log.domain.LogService;
import com.cherrypick.backend.global.log.domain.port.LogAppender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ExceptionLogService {


    private final LogAppender logAppender;
    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 애플리케이션에서 발생한 예외 및 오류 로그를 기록합니다.
     * <p>
     * GlobalExceptionHandler에서 캐치된 모든 예외에 대해 HTTP 상태 코드, 에러 메시지, 스택 트레이스를 추적합니다.
     * 환경별로 스택 트레이스 상세도를 차등 적용하여 운영 환경에서는 로그 크기를 최적화합니다.
     * </p>
     *
     * <h3>환경별 스택 트레이스 처리:</h3>
     * <ul>
     *   <li><b>local</b>: 전체 스택 트레이스 콘솔 출력 (디버깅 용이)</li>
     *   <li><b>prod</b>: 상위 10개 프레임만 로그 전송 (성능 최적화)</li>
     *   <li><b>dev</b>: 전체 스택 트레이스 로그 전송</li>
     * </ul>
     *
     * @param status HTTP 응답 상태 코드 (예: 400 Bad Request, 500 Internal Server Error)
     * @param msg 에러 메시지 (사용자에게 전달되는 메시지 또는 내부 에러 설명)
     * @param stackTrace 예외 발생 지점의 스택 트레이스 배열
     */
    public void errorLog(HttpStatus status, String msg, StackTraceElement[] stackTrace) {

        var stackTraceList = Arrays.asList(stackTrace);
        if(env.equals("prod") || env.equals("dev")) stackTraceList = stackTraceList.subList(0, Math.min(10, stackTraceList.size()));
        if(env.equals("local")) Arrays.stream(stackTrace).forEach(System.out::println);

        var stackTraceString = stackTraceList.stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")) ;

        HashMap<String, Object> map = new HashMap<>();
        map.put("error_msg", String.valueOf(msg));
        map.put("error_status", String.valueOf(status));
        map.put("error_trace", stackTraceString);

        logAppender.appendError("ERROR_LOG", map);

    }

    /**
     * 예상하지 못한 시스템 예외 로그를 기록합니다.
     * <p>
     * GlobalExceptionHandler에서 처리되지 않은 예외나 시스템 레벨 오류를 추적합니다.
     * errorLog와 분리하여 비즈니스 오류와 시스템 예외를 구분하여 모니터링할 수 있습니다.
     * </p>
     *
     * <h3>errorLog와의 차이점:</h3>
     * <ul>
     *   <li><b>errorLog</b>: 예상된 비즈니스 오류 (유효성 검증 실패, 권한 오류 등)</li>
     *   <li><b>exceptionLog</b>: 예상하지 못한 시스템 예외 (NullPointerException, 외부 API 장애 등)</li>
     * </ul>
     *
     * <h3>환경별 스택 트레이스 처리:</h3>
     * <ul>
     *   <li><b>local</b>: 전체 스택 트레이스 콘솔 출력 (디버깅 용이)</li>
     *   <li><b>prod/dev</b>: 상위 10개 프레임만 로그 전송 (성능 최적화)</li>
     * </ul>
     *
     * @param status HTTP 응답 상태 코드 (일반적으로 500 Internal Server Error)
     * @param msg 예외 메시지 (시스템 예외 설명)
     * @param stackTrace 예외 발생 지점의 스택 트레이스 배열
     */
    public void exceptionLog(HttpStatus status, String msg, StackTraceElement[] stackTrace) {

        var stackTraceList = Arrays.asList(stackTrace);
        if(env.equals("prod") || env.equals("dev")) stackTraceList = stackTraceList.subList(0, Math.min(10, stackTraceList.size()));
        if(env.equals("local")) Arrays.stream(stackTrace).forEach(System.out::println);

        var stackTraceString = stackTraceList.stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")) ;

        HashMap<String, Object> map = new HashMap<>();
        map.put("exception_msg", String.valueOf(msg));
        map.put("exception_status", String.valueOf(status));
        map.put("exception_trace", stackTraceString);

        logAppender.appendError("EXCEPTION_LOG", map);

    }

}
