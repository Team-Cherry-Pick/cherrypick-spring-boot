package com.cherrypick.backend.global.util;


import com.cherrypick.backend.domain.oauth.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.oauth.dto.UserEnvDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component @Slf4j
public class JWTUtil
{
    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final SecretKey registerSecretKey;

    public final Long accessValidPeriod;
    public final Long refreshValidPeriod;
    public final Long registerValidPeriod;

    public JWTUtil(@Value("${spring.jwt.access.key}") String accessSecret ,
                   @Value("${spring.jwt.refresh.key}") String refreshSecret,
                   @Value("${spring.jwt.register.key}") String registerSecretKey,
                   @Value("${spring.jwt.access.period}") long accessValidPeriod,
                   @Value("${spring.jwt.refresh.period}") long refreshValidPeriod,
                   @Value("${spring.jwt.register.period}") long registerValidPeriod) {

        this.accessSecretKey = new SecretKeySpec(accessSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshSecretKey = new SecretKeySpec(refreshSecret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.registerSecretKey = new SecretKeySpec(registerSecretKey.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.accessValidPeriod = accessValidPeriod;
        this.refreshValidPeriod = refreshValidPeriod;
        this.registerValidPeriod = registerValidPeriod;
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


    public String removeBearer(String token)
    {
        return token.replace("Bearer ", "");
    }

    // 레지스터 토큰에서 데이터를 추출
    public AuthResponseDTOs.RegisterTokenDTO getRegisterTokenPayload(String registerToken) {

        var oauthId = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("oauthId", String.class);;
        var provider = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("provider", String.class);
        var userEnv = Jwts.parser().verifyWith(registerSecretKey).build().parseSignedClaims(registerToken).getPayload().get("userEnv", String.class);

        return AuthResponseDTOs.RegisterTokenDTO.builder()
                .oauthId(oauthId)
                .provider(provider)
                .userEnv(UserEnvDTO.fromJson(userEnv))
                .build()
                ;
    }

    // 엑세스 토큰에서 데이터를 추출
    public AuthenticationDetailDTO getUserDetailDTOFromAccessToken(String accessToken) {
        // TODO : 다중 권한 구조로 전환 필요
        accessToken = removeBearer(accessToken);
        var userId = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("userId", Long.class);;
        var roles = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("role", String.class);
        var nickName = Jwts.parser().verifyWith(accessSecretKey).build().parseSignedClaims(accessToken).getPayload().get("nickname", String.class);

        return AuthenticationDetailDTO.builder()
                .userId(userId)
                .nickname(nickName)
                .role(Role.valueOf(roles))
                .build();
    }

    public Long getUserIdFromRefreshToken(String token) {

        try{
            return Jwts.parser().verifyWith(refreshSecretKey).build().parseSignedClaims(token).getPayload().get("userId", Long.class);
        } catch (Exception e) {
            throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);
        }
    }

    // 레지스터 토큰 생성
    public String createRegisterToken(AuthResponseDTOs.RegisterTokenDTO dto) {
        return Jwts.builder()
                .claim("oauthId", dto.oauthId())
                .claim("provider", dto.provider())
                .claim("userEnv", dto.userEnv().toJson())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + registerValidPeriod))
                .signWith(registerSecretKey)
                .compact();
    }

    // 엑세스 토큰 생성
    // TODO : 다중 권한 구조로 전환 필요
    public String createAccessToken(Long userId, Role role, String nickname) {

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

    // 리프레시 토큰 생성
    public String createRefreshToken(long userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("state", UUID.randomUUID().toString())
                .claim("type", "refresh")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshValidPeriod))
                .signWith(refreshSecretKey)
                .compact();
    }

    // TODO : https로 전환 시 sameSite를 바꿔줘야함.
    // 리프레시 토큰을 쿠키로 바꿔줌.
    public ResponseCookie createRefreshCookie(String refreshToken){
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                //.sameSite("Strict")
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
    }


}