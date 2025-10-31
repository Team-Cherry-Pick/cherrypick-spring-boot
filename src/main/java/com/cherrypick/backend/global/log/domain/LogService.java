package com.cherrypick.backend.global.log.domain;


import com.cherrypick.backend.global.log.domain.port.LogAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 구조화된 로그를 생성하고 외부 로깅 시스템(Logstash)으로 전송하는 서비스.
 * <p>
 * 애플리케이션의 모든 로그를 MDC(Mapped Diagnostic Context)를 활용하여
 * JSON 형태로 구조화하고, ELK Stack을 통해 중앙 집중식 로그 관리를 지원합니다.
 * </p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>액세스 로그 - API 요청/응답 추적</li>
 *   <li>로그인 로그 - 사용자 인증 이벤트</li>
 *   <li>에러 로그 - 예외 및 오류 추적 (스택 트레이스 포함)</li>
 *   <li>사용자 활동 로그 - 회원가입, 탈퇴, 구매 클릭, 공유 클릭 등</li>
 *   <li>외부 API 사용 로그 - OpenAI 토큰 사용량 등</li>
 * </ul>
 *
 * <h3>로그 설계 원칙:</h3>
 * <ul>
 *   <li><b>필드명 변경 금지</b> - Logstash 파이프라인과 연동되어 있어 필드명 변경 시 장애 발생</li>
 *   <li><b>Null-Safe</b> - 모든 파라미터는 null 안전하게 처리 (Optional.ofNullable 사용)</li>
 *   <li><b>환경 분리</b> - local/dev/prod 환경별 로그 레벨 및 상세도 차등 적용</li>
 * </ul>
 *
 * @since 1.0
 * @see org.slf4j.MDC
 * @see com.cherrypick.backend.global.log.domain.port.LogAppender
 */
@Slf4j @Component @RequiredArgsConstructor
public class LogService {


    private final LogAppender logAppender;
    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 애플리케이션 시작 시 서버 시작 로그를 기록합니다.
     * <p>
     * Spring Bean 초기화 완료 후 자동으로 실행되며,
     * 서버 구동 시점을 추적하기 위한 로그를 생성합니다.
     * </p>
     */
    @PostConstruct
    public void init() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("start_msg", "Starting Server Application");
        logAppender.appendInfo("SERVER_START_LOG", map);

    }

    /**
     * API 요청에 대한 액세스 로그를 기록합니다.
     * <p>
     * 모든 HTTP 요청에 대해 요청 처리 시간, URI 패턴, 사용자 정보, HTTP 메서드, 클라이언트 IP 등을 추적합니다.
     * </p>
     *
     * @param durationTime 요청 처리 시간 (밀리초)
     * @param uriPattern Spring MVC URI 패턴 (예: /api/deals/{dealId})
     * @param userId 사용자 ID (비로그인 시 null)
     * @param deviceId 디바이스 고유 식별자
     * @param method HTTP 메서드 (GET, POST, PUT, DELETE 등)
     * @param clientIp 클라이언트 IP 주소
     * @param url 실제 요청 URL
     * @param queryString 쿼리 파라미터 문자열
     */
    public void accessLog(Long durationTime, String uriPattern, Long userId, String deviceId,  String method, String clientIp, String url, String queryString) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("access_duration", Optional.ofNullable(durationTime).orElse(-1L));
        map.put("access_uriPattern", Optional.ofNullable(uriPattern).orElse("unknown"));
        map.put("access_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("access_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("access_method", Optional.ofNullable(method).orElse("unknown"));
        map.put("access_clientIp", Optional.ofNullable(clientIp).orElse("unknown"));
        map.put("access_queryString", Optional.ofNullable(queryString).orElse("unknown"));
        map.put("access_url", Optional.ofNullable(url).orElse("unknown"));

        logAppender.appendInfo("ACCESS_LOG", map);

    }

    /**
     * 사용자 로그인 이벤트 로그를 기록합니다.
     * <p>
     * OAuth 2.0 기반 소셜 로그인 시 신규 가입 여부, OAuth 제공자, 사용자 정보, 클라이언트 환경 등을 추적합니다.
     * </p>
     *
     * @param isNewUser 신규 가입 여부 (true: 첫 로그인, false: 기존 사용자)
     * @param provider OAuth 제공자 (예: "google", "kakao", "naver")
     * @param userId 사용자 고유 ID
     * @param deviceId 디바이스 고유 식별자
     * @param os 운영체제 정보 (예: "iOS 16.0", "Android 13")
     * @param browser 브라우저 정보 (예: "Chrome 120", "Safari 17")
     * @param version 앱 버전 (모바일 앱인 경우)
     */
    public void loginLog(Boolean isNewUser, String provider, Long userId, String deviceId, String os, String browser, String version)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("login_isNewUser", Optional.ofNullable(isNewUser).orElse(false));
        map.put("login_provider", Optional.ofNullable(provider).orElse("unknown"));
        map.put("login_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("login_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("login_os", Optional.ofNullable(os).orElse("unknown"));
        map.put("login_browser", Optional.ofNullable(browser).orElse("unknown"));
        map.put("login_version", Optional.ofNullable(version).orElse("unknown"));

        logAppender.appendInfo("LOGIN_LOG", map);

    }

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

    /**
     * 사용자 회원 탈퇴 이벤트 로그를 기록합니다.
     * <p>
     * GDPR 및 개인정보보호법 준수를 위해 탈퇴 사용자 정보를 추적합니다.
     * </p>
     *
     * @param userId 탈퇴 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param oauthId OAuth 제공자 고유 ID
     * @param message 탈퇴 사유 또는 추가 메시지
     */
    public void userDeleteLog(Long userId, String name, String email, String oauthId, String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("deluser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("deluser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("deluser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("deluser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("deluser_email", Optional.ofNullable(email).orElse("unknown"));

        logAppender.appendInfo("USER_DELETE_LOG", map);
    }

    /**
     * 신규 사용자 회원가입 이벤트 로그를 기록합니다.
     * <p>
     * 가입자 유입 경로 분석 및 사용자 증가 추이 파악에 활용됩니다.
     * </p>
     *
     * @param userId 신규 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param oauthId OAuth 제공자 고유 ID
     * @param message 가입 경로 또는 추가 메시지
     */
    public void userRegisterLog(Long userId, String name, String email, String oauthId, String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("reguser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("reguser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("reguser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("reguser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("reguser_email", Optional.ofNullable(email).orElse("unknown"));

        logAppender.appendInfo("USER_REGISTER_LOG", map);
    }

    /**
     * 딜 상세 페이지에서 구매 버튼 클릭 이벤트 로그를 기록합니다.
     * <p>
     * 전환율 분석, 인기 카테고리 파악, 사용자별 구매 패턴 분석에 활용됩니다.
     * </p>
     *
     * @param userId 사용자 ID (비로그인 시 null)
     * @param deviceId 디바이스 고유 식별자
     * @param dealId 딜 ID
     * @param dealTitle 딜 제목
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리 이름
     */
    public void clickPurchaseLog(Long userId,
                                 String deviceId,
                                 Long dealId,
                                 String dealTitle,
                                 Long categoryId,
                                 String categoryName)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("purchaseclick_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("purchaseclick_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("purchaseclick_dealId", Optional.ofNullable(dealId).orElse(-1L));
        map.put("purchaseclick_dealTitle", Optional.ofNullable(dealTitle).orElse("unknown"));
        map.put("purchaseclick_categoryId", Optional.ofNullable(categoryId).orElse(-1L));
        map.put("purchaseclick_categoryName", Optional.ofNullable(categoryName).orElse("unknown"));

        logAppender.appendInfo("PURCHASE_CLICK_LOG", map);
    }

    /**
     * 딜 공유 버튼 클릭 이벤트 로그를 기록합니다.
     * <p>
     * 바이럴 확산 추이 분석, 공유율 높은 딜 파악, 사용자 참여도 측정에 활용됩니다.
     * </p>
     *
     * @param userId 사용자 ID (비로그인 시 null)
     * @param deviceId 디바이스 고유 식별자
     * @param dealId 딜 ID
     * @param dealTitle 딜 제목
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리 이름
     */
    public void clickShareLog(Long userId,
                                 String deviceId,
                                 Long dealId,
                                 String dealTitle,
                                 Long categoryId,
                                 String categoryName)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("shareclick_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("shareclick_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("shareclick_dealId", Optional.ofNullable(dealId).orElse(-1L));
        map.put("shareclick_dealTitle", Optional.ofNullable(dealTitle).orElse("unknown"));
        map.put("shareclick_categoryId", Optional.ofNullable(categoryId).orElse(-1L));
        map.put("shareclick_categoryName", Optional.ofNullable(categoryName).orElse("unknown"));

        logAppender.appendInfo("SHARE_CLICK_LOG", map);
    }


    /**
     * OpenAI API 사용량 로그를 기록합니다.
     * <p>
     * GPT 모델 사용에 따른 토큰 소비량을 추적하여 API 사용 비용 모니터링 및 프롬프트 최적화에 활용됩니다.
     * </p>
     *
     * @param promptTokens 입력 프롬프트 토큰 수
     * @param completionTokens 응답 생성 토큰 수
     * @param totalTokens 총 사용 토큰 수 (promptTokens + completionTokens)
     */
    public void openAiLog(Integer promptTokens, Integer completionTokens, Integer totalTokens)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("prompt_tokens", Optional.ofNullable(promptTokens).orElse(-1) );
        map.put("completion_tokens", Optional.ofNullable(completionTokens).orElse(-1));
        map.put("total_tokens", Optional.ofNullable(totalTokens).orElse(-1));

        logAppender.appendInfo("OPENAI_LOG", map);
    }

}
