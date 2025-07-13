package com.cherrypick.backend.domain.auth.infra.jwt;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.global.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AccessTokenProvider
{

    private final SecretKey accessSecretKey;
    public final Long accessValidPeriod;
    public final ObjectMapper mapper = new ObjectMapper();

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

    // 엑세스 토큰 생성
    // TODO : 다중 권한 구조로 전환 필요
    public String createToken(Long userId, Role role, String nickname) {

        return Jwts.builder()
                .claim("userId", userId)
                .claim("nickname", nickname)
                .claim("role", role.toString())
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessValidPeriod))
                .signWith(accessSecretKey)
                .compact();

    }

    // 엑세스 토큰에서 데이터를 추출
    public AuthenticatedUser getAuthenticatedUser(String accessToken) {
        // TODO : 다중 권한 구조로 전환 필요
        accessToken = JwtUtil.removeBearer(accessToken);

        var userId = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("userId", Long.class);;
        var roles = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("role", String.class);
        var nickName = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("nickname", String.class);

        return AuthenticatedUser.builder()
                .userId(userId)
                .nickname(nickName)
                .role(Role.valueOf(roles))
                .build();
    }


}
