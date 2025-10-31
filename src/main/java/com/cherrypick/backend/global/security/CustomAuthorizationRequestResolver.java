package com.cherrypick.backend.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component @Slf4j
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    /// OAuth2 요청을 받아 처리하는 커스텀 클래스.
    /// 사용자 정보를 받기 위해 커스텀 했음.
    /// 주요 프로세스 : 요청을 받아 파라미터를 추출 후, 토큰 획득 후 처리할 수 있도록 state에 붙여 카카오로 전송.


    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
        return customize(authRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customize(authRequest, request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authRequest, HttpServletRequest request) {

        if (authRequest == null) return null;

        log.trace(":::: 신규 로그인 요청이 발생하였습니다 ::::");

        // 사용자에게서 얻어낸 데이터를 추출.
        String redirect = request.getParameter("redirect"); // redirect : 유저가 이전에 방문했던 페이지. 로그인 성공 시 해당 URL 로 리다이렉트
        String deviceId = Optional.ofNullable(request.getParameter("deviceId")).orElse("default"); // 기기 식별용 Id, nullable
        String os = Optional.ofNullable(request.getParameter("os")).orElse(""); // 해당 기기의 OS, nullable
        String browser = Optional.ofNullable(request.getParameter("browser")).orElse(""); // 브라우저 정보, nullable
        String version = Optional.ofNullable(request.getParameter("version")).orElse(""); // 브라우저의 버전, nullable
        String origin = Optional.ofNullable(request.getParameter("origin")).orElse(""); // 브라우저의 버전, nullable
        String uuid = UUID.randomUUID().toString();

        // 데이터를 조인해서 문자열 덩어리로 만들어줌.
        // 컴파일 단계에서 Stringbuilder로 전환되기 때문에 join보다 빠름.
        var infoStr = origin + "|" +  redirect + "|" + deviceId + "|" + os + "|" + browser + "|" + version + "|" + uuid;

        // url을 Base64로 인코딩하여 state에 붙여줌.
        String encodedInfo = Base64.getUrlEncoder().encodeToString(infoStr.getBytes());

        // state 값에 카카오에 날려줌.
        return OAuth2AuthorizationRequest.from(authRequest)
                    .state(encodedInfo)
                    .build();


    }
}
