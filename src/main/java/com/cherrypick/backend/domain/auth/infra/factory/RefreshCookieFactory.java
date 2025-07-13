package com.cherrypick.backend.domain.auth.infra.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component @RequiredArgsConstructor
public class RefreshCookieFactory
{
    @Value("${spring.jwt.refresh.key}")
    private Long refreshValidPeriod;

    public ResponseCookie createRefreshCookie(String value)
    {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofSeconds(refreshValidPeriod))
            .build();
    }

    public ResponseCookie createExpiringRefreshCookie(String value)
    {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();
    }

}
