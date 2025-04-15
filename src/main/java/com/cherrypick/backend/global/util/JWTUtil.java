package com.cherrypick.backend.global.util;


import com.cherrypick.backend.domain.user.entity.Role;
import com.cherrypick.backend.domain.user.dto.UserDetailDTO;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component @Slf4j
public class JWTUtil
{
    private final SecretKey secretKey;
    private final long accessValidPeriod = 60 * 60 * 60L;
    private final long refreshValidPeriod = 60 * 60 * 24 * 7L;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getUserId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
    }

    private String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    private String getNickname(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("nickname", String.class);
    }

    public Boolean isExpired(String token) {

        try{
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }

    }

    public UserDetailDTO getUserDetailDTOFromAccessToken(String accessToken) {

        accessToken = removeBearer(accessToken);

        return UserDetailDTO.builder()
                .userId(getUserId(accessToken))
                .nickname(getNickname(accessToken))
                .role(Role.valueOf(getRole(accessToken)))
                .build();
    }

    public String removeBearer(String token)
    {
        return token.replace("Bearer ", "");
    }


    public String createAccessToken(Long userId, Role role, String nickname) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("role", role.toString())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessValidPeriod))
                .signWith(secretKey)
                .compact();

    }

    public String createRefreshToken(long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("state", UUID.randomUUID().toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshValidPeriod))
                .signWith(secretKey)
                .compact();
    }

    public Cookie createRefreshCookie(String value){
        var cookie = new Cookie("refresh", value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }

}
