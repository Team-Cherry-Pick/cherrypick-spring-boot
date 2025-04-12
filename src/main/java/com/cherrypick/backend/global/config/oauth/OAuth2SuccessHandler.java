package com.cherrypick.backend.global.config.oauth;

import com.cherrypick.backend.global.util.JWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component @Slf4j @RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2UserDTO userInfo = (OAuth2UserDTO) authentication.getPrincipal();
        log.info(authentication.getDetails().toString());
        String jwtToken = jwtProvider.createJwt(userInfo.userId(), userInfo.role(), userInfo.nickname());

        String redirectUrl = request.getRequestURI();
        log.info(redirectUrl);

        response.addCookie(createCookie(jwtToken));
        
    }

    public Cookie createCookie(String jwtToken)
    {
        var cookie = new Cookie("Authorization", jwtToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(3600);
        //cookie.setSecure(true); https에서만 작동하도록 하는 코드. CI/CD에서는

        return cookie;

    }

}
