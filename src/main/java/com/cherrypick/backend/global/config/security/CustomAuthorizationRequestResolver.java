package com.cherrypick.backend.global.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component @Slf4j
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

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

        // redirect : 유저가 이전에 방문했던 페이지. 로그인 성공 시 해당 URL 로 리다이렉트
        // url을 Base64로 인코딩하여 state에 붙여줌.
        String redirect = Base64.getUrlEncoder().encodeToString(request.getParameter("redirect").getBytes());

        // state 값에 저장해 보내줌.
        if (redirect != null)
            return OAuth2AuthorizationRequest.from(authRequest)
                    .state(redirect)
                    .build();

        return authRequest;
    }
}
