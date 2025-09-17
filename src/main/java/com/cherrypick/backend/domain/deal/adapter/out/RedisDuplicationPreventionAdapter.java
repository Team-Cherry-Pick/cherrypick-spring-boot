package com.cherrypick.backend.domain.deal.adapter.out;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 중복 방지 전담 어댑터
 * 조회, 구매, 추천 등의 중복 액션을 Redis로 방지
 */
@Component
@RequiredArgsConstructor
public class RedisDuplicationPreventionAdapter {

    private final RedisTemplate<String, Object> redisTemplate;

    // TTL 설정 (24시간)
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);


    // TODO : behavior 를 쓸 일이 생기면 그때 빼기
    public enum Behavior{
        VIEW, PURCHASE, SHARE;

        public String generateKey(String deviceId, Long dealId){

            return "REPIK:duplication_prevention:" + this.name() + ":" + dealId + ":" + deviceId;
        }
    }

    /**
     * 조회수 중복 방지 확인
     * @param behavior 행동 Enum
     * @param dealId 게시물 ID
     * @param deviceId 디바이스 ID
     * @return 이미 조회한 경우 true, 처음 조회하는 경우 false
     */
    public boolean isDuplicate(Behavior behavior, Long dealId, String deviceId) {
        if(deviceId == null || deviceId.isEmpty()){ return false; }
        String key = behavior.generateKey(deviceId, dealId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 기록 저장
     * @param behavior 행동 Enum
     * @param dealId 게시물 ID
     * @param deviceId 디바이스 ID
     */
    public void preventDuplicate(Behavior behavior, Long dealId, String deviceId) {
        if(deviceId == null || deviceId.isEmpty()){
            deviceId = "unknown";
        }
        String key = behavior.generateKey(deviceId, dealId);
        redisTemplate.opsForValue().set(key, "DONE", DEFAULT_TTL);
    }

}