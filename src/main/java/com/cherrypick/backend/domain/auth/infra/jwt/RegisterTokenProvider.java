package com.cherrypick.backend.domain.auth.infra.jwt;

import com.cherrypick.backend.domain.auth.domain.vo.UserEnv;
import com.cherrypick.backend.domain.auth.domain.vo.token.RegisterTokenPayload;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.UserEnvDTO;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class RegisterTokenProvider {

    private final SecretKey registerSecretKey;
    private final Long registerValidPeriod;


    public RegisterTokenProvider(@Value("${spring.jwt.register.key}") String registerSecret, @Value("${spring.jwt.register.period}") long registerValidPeriod){
        this.registerSecretKey = new SecretKeySpec(registerSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.registerValidPeriod = registerValidPeriod;
    }

    // 레지스터 토큰 생성
    public String createToken(String oauthId, String provider, String deviceId, String os, String browser, String version) {
        return Jwts.builder()
                .claim("oauthId", oauthId)
                .claim("provider", provider)
                .claim("deviceId", deviceId)
                .claim("os", os)
                .claim("browser", browser)
                .claim("version", version)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + registerValidPeriod * 1000))
                .signWith(registerSecretKey)
                .compact();
    }

    // 레지스터 토큰 파싱
    // 레지스터 토큰에서 데이터를 추출
    public RegisterTokenPayload getTokenPayload(String registerToken) {

        var oauthId = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("oauthId", String.class);;
        var provider = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("provider", String.class);
        var deviceId = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("deviceId", String.class);
        var os = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("os", String.class);
        var browser = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("browser", String.class);
        var version = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("version", String.class);

        var userEnv = UserEnv.builder()
                .deviceId(deviceId)
                .os(os)
                .browser(browser)
                .version(version)
                .build();

        return new RegisterTokenPayload(oauthId, provider, userEnv);
    }

}
