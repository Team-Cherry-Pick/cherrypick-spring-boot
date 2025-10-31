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
