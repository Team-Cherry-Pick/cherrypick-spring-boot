package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.dto.response.UserBehaviorLogListDTO;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.deal.repository.HashTagRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Range;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

    // 모든 로그를 읽어와
    public UserBehaviorLogListDTO getLogByUserId(Long userId)
    {
        log.info("로그조회 시작");
        StreamOperations<String, String, String> streamOps = redisTemplate.opsForStream();
        // 모든 메시지 조회: ID 범위 "-" to "+"
        List<MapRecord<String, String, String>> records = streamOps.range(STREAM_NAME, Range.unbounded());
        var responseList = new ArrayList<UserBehaviorLogListDTO.UserBehaviorLogDTO>();

        var userLogList =
                records.stream()
                .filter(record -> Long.valueOf(record.getValue().get("userId")).equals(userId))
                        .toList();

        for (MapRecord<String, String, String> record : userLogList)
        {
            var id = record.getId().getValue();
            var map = record.getValue();

            String timestamp = id.split("-")[0];
            LocalDateTime createdAt = Instant.ofEpochMilli(Long.parseLong(timestamp))
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();

            var title = dealRepository.findById(Long.parseLong(record.getValue().get("dealId")))
                    .map(Deal::getTitle)
                    .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

            var userBehaviorLogDTO = UserBehaviorLogListDTO.UserBehaviorLogDTO.builder()
                    .logId(id)
                    .targetDealId(map.get("dealId"))
                    .targetDealTitle(title)
                    .type(map.get("type"))
                    .createdAt(createdAt.toString())
                    .build();

            responseList.add(userBehaviorLogDTO);
        }

        log.info("로그조회 끝");
        return new UserBehaviorLogListDTO(responseList);

    }

}
