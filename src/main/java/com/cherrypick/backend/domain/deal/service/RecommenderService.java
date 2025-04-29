package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.deal.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Slf4j
public class RecommenderService
{
    private final RedisTemplate<String, Object> redisTemplate;
    private final DealRepository dealRepository;
    private final HashTagRepository hashTagRepository;
    final String STREAM_NAME = "USER_BEHAVIOR_STREAM";

    public String addUserBehaviorLog(DealRequestDTOs.UserBehaviorDTO behaviorDTO)
    {
        log.info(behaviorDTO.toString());
        var rId = redisTemplate.opsForStream().add(STREAM_NAME, behaviorDTO.toMap());
        log.info(rId.getValue());

        return rId.getValue();
    }

}
