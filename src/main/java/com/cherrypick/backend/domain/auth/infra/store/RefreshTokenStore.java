package com.cherrypick.backend.domain.auth.infra.store;

import com.cherrypick.backend.domain.auth.domain.vo.UserEnv;
import com.cherrypick.backend.domain.auth.domain.vo.token.RefreshTokenPayload;
import com.cherrypick.backend.domain.auth.presentation.dto.UserEnvDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.CacheKeyUtil;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component @RequiredArgsConstructor
public class RefreshTokenStore
{
    @Value("${spring.jwt.refresh.key}")
    private Long refreshValidPeriod;
    private final RedisTemplate<String, Object> redisTemplate;

    public void initializeToken(Long userId, String deviceId, String refreshToken, UserEnv userEnv)
    {
        String key = CacheKeyUtil.getRefreshTokenKey(userId, deviceId);
        redisTemplate.opsForHash().put(key, "token", refreshToken);
        redisTemplate.opsForHash().put(key, "deviceId", userEnv.deviceId());
        redisTemplate.opsForHash().put(key, "os", userEnv.os());
        redisTemplate.opsForHash().put(key, "browser", userEnv.browser());
        redisTemplate.opsForHash().put(key, "version", userEnv.version());
        redisTemplate.expire(key, Duration.ofSeconds(refreshValidPeriod));

    }

    public void saveToken(Long userId, String deviceId, String refreshToken)
    {
        String key = CacheKeyUtil.getRefreshTokenKey(userId, deviceId);
        redisTemplate.opsForHash().put(key, "token", refreshToken);
        redisTemplate.expire(key, Duration.ofSeconds(refreshValidPeriod));
    }

    public String loadToken(Long userId, String deviceId)
    {
        String key = CacheKeyUtil.getRefreshTokenKey(userId, deviceId);
        if (!redisTemplate.hasKey(key)) throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);
        var token = Optional.ofNullable((String) redisTemplate.opsForHash().get( key, "token"));

        return token.orElseThrow(() -> new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    public UserEnv loadUserEnv(Long userId, String deviceId) {
        String key = CacheKeyUtil.getRefreshTokenKey(userId, deviceId);

        var deviceIdVal = redisTemplate.opsForHash().get(key, "deviceId");
        var os = redisTemplate.opsForHash().get(key, "os");
        var browser = redisTemplate.opsForHash().get(key, "browser");
        var version = redisTemplate.opsForHash().get(key, "version");

        if (deviceIdVal == null || os == null || browser == null || version == null)
            throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND);

        return UserEnv.builder()
                .deviceId((String) deviceIdVal)
                .os((String) os)
                .browser((String) browser)
                .version((String) version)
                .build();
    }

}
