package com.cherrypick.backend.global.security;

import com.cherrypick.backend.global.log.domain.port.LogAppender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class SecurityLogService
{

    private final LogAppender logAppender;

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
        map.put("duration", Optional.ofNullable(durationTime).orElse(-1L));
        map.put("uriPattern", Optional.ofNullable(uriPattern).orElse("unknown"));
        map.put("userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("method", Optional.ofNullable(method).orElse("unknown"));
        map.put("clientIp", Optional.ofNullable(clientIp).orElse("unknown"));
        map.put("queryString", Optional.ofNullable(queryString).orElse("unknown"));
        map.put("url", Optional.ofNullable(url).orElse("unknown"));

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
        map.put("isNewUser", Optional.ofNullable(isNewUser).orElse(false));
        map.put("provider", Optional.ofNullable(provider).orElse("unknown"));
        map.put("userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("os", Optional.ofNullable(os).orElse("unknown"));
        map.put("browser", Optional.ofNullable(browser).orElse("unknown"));
        map.put("clientVersion", Optional.ofNullable(version).orElse("unknown"));

        logAppender.appendInfo("LOGIN_LOG", map);

    }


}
