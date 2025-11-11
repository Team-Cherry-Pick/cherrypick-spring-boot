package com.cherrypick.backend.domain.auth.infra.jwt;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccessTokenProvider
{

    private final SecretKey accessSecretKey;
    private final Long accessValidPeriod;
    private final ObjectMapper mapper = new ObjectMapper();

    public AccessTokenProvider(@Value("${spring.jwt.access.key}") String accessSecret, @Value("${spring.jwt.access.period}") long accessValidPeriod) {

        this.accessSecretKey = new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessValidPeriod = accessValidPeriod;
    }

    public Boolean isExpired(String token) {
        try{
            return Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date getExpriationTime(String token) {
        try{
            return Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(token).getPayload().getExpiration();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * 액세스 토큰 생성 (다중 권한 지원)
     * @param roles 역할 집합 (예: Set.of("ADMIN", "CLIENT"))
     */
    public String createToken(Long userId, Set<String> roles, String nickname) {
        // roles를 comma-separated string으로 저장
        String rolesStr = String.join(",", roles);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("roles", rolesStr)  // "ADMIN,CLIENT" 형태
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessValidPeriod * 1000))
                .signWith(accessSecretKey)
                .compact();
    }

    /**
     * 액세스 토큰에서 인증 정보 추출 (다중 권한 지원)
     */
    public AuthenticatedUser getAuthenticatedUser(String accessToken) {
        accessToken = JwtUtil.removeBearer(accessToken);

        Claims claims = Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        Long userId = claims.get("userId", Long.class);
        // 기존 유저들의 토큰은 role 이었음.
        String rolesStr = claims.get("roles", String.class) == null ? claims.get("role", String.class) : claims.get("roles", String.class);
        String nickName = claims.get("nickname", String.class);

        // "ADMIN,CLIENT" 문자열을 Set<String>으로 변환
        Set<String> roles = Arrays.stream(rolesStr.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        return AuthenticatedUser.builder()
                .userId(userId)
                .nickname(nickName)
                .roles(roles)
                .build();
    }
}
