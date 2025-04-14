package com.cherrypick.backend.global.util;


import com.cherrypick.backend.domain.user.entity.Role;
import com.cherrypick.backend.global.config.oauth.OAuth2UserDTO;
import com.cherrypick.backend.global.config.oauth.UserDetailDTO;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component @Slf4j
public class JWTUtil
{
    private final SecretKey secretKey;
    private final long validPeriod = 60 * 60 * 60L;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {

        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public Long getUserId(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getNickname(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("nickname", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public UserDetailDTO getUserDetailDTO(String accessToken) {
        return UserDetailDTO.builder()
                .userId(getUserId(accessToken))
                .nickname(getNickname(accessToken))
                .role(Role.valueOf(getRole(accessToken)))
                .build();
    }


    public String createAccessToken(Long userId, Role role, String nickname) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("role", role.toString())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + validPeriod))
                .signWith(secretKey)
                .compact();

    }

}
