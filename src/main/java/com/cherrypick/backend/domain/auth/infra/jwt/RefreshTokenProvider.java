package com.cherrypick.backend.domain.auth.infra.jwt;

import com.cherrypick.backend.domain.auth.domain.vo.token.RefreshTokenPayload;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class RefreshTokenProvider
{

    private final SecretKey refreshSecretKey;
    private final Long refreshValidPeriod;


    public RefreshTokenProvider(@Value("${spring.jwt.refresh.key}") String refreshSecret, @Value("${spring.jwt.refresh.period}") long refreshValidPeriod){
        this.refreshSecretKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshValidPeriod = refreshValidPeriod;
    }

    // 리프레시 토큰 문자열 생성
    public String createToken(Long userId, String deviceId){
        return Jwts.builder()
                .claim("userId", userId)
                .claim("deviceId", deviceId)
                .claim("state", UUID.randomUUID().toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshValidPeriod * 1000))
                .signWith(refreshSecretKey)
                .compact();
    }

    // 리프레시 토큰 값을 반환
    public RefreshTokenPayload getPayload(String token) {
        try{
            var userId = Jwts.parser().verifyWith(refreshSecretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
            var deviceId = Jwts.parser().verifyWith(refreshSecretKey).build().parseSignedClaims(token).getPayload().get("deviceId", String.class);

            return new RefreshTokenPayload(userId, deviceId);

        } catch (Exception e) {
            throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);
        }

    }

}
