package com.cherrypick.backend.global.config.security;

import com.cherrypick.backend.domain.oauth.dto.OAuth2LoginSuccessResponseDTO;
import com.cherrypick.backend.domain.oauth.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.global.util.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Component @Slf4j @RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    @Value("${spring.userInfoUrl}")
    String userInfoUpdateURL;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2UserDTO userInfo = (OAuth2UserDTO) authentication.getPrincipal();

        String redirectUrl = new String(Base64.getUrlDecoder().decode(request.getParameter("state")));
        if(userInfo.isNewUser()) redirectUrl = userInfoUpdateURL;

        // 엑세스 토큰과 리프레시 토큰
        String accessToken = "Bearer " + jwtUtil.createAccessToken(userInfo.userId(), userInfo.role(), userInfo.nickname());
        String refreshToken = jwtUtil.createRefreshToken(userInfo.userId());

        // 리프레시 토큰은 쿠키로 담아줌. (서버에서 읽을 수만 있으면 장땡
        response.addCookie(createRefreshCookie(refreshToken));
        // 리프레시 토큰을 Redis에 저장함.
        authService.saveResfreshToken(userInfo.userId(), refreshToken);

        var responseDTO = OAuth2LoginSuccessResponseDTO.builder()
                .userId(userInfo.userId())
                .accessToken(accessToken)
                .isNewUser(userInfo.isNewUser())
                .redirectURL(redirectUrl)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(responseDTO));

    }

    private Cookie createRefreshCookie(String value){
        var cookie = new Cookie("refresh", value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }


}
