package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.UserBehaviorLogListDTO;
import com.cherrypick.backend.domain.deal.dto.response.UserInterestListDTO;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.entity.HashTag;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.deal.repository.HashTagRepository;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class RecommenderService
{
    private final RedisTemplate<String, Object> redisTemplate;
    private final DealRepository dealRepository;
    private final HashTagRepository hashTagRepository;
    private final ImageRepository imageRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    final String STREAM_NAME = "USER_BEHAVIOR_STREAM";

    // 유저 행동 로그 생성
    public String addUserBehaviorLog(DealRequestDTOs.UserBehaviorDTO behaviorDTO)
    {
        log.info(behaviorDTO.toString());
        var rId = redisTemplate.opsForStream().add(STREAM_NAME, behaviorDTO.toMap());
        log.info(rId.getValue());

        return rId.getValue();
    }

    // 모든 로그를 읽어와 유저의 것으로 정제 후 반환
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

        var dealIdList = userLogList.stream().map(Record::getValue)
                .map(m -> Long.valueOf(m.get("dealId")))
                .toList();

        var dealMap = dealRepository.findAllById(dealIdList).stream().collect(Collectors.toMap(
                Deal::getDealId,
                Deal::getTitle
        ));

        for (MapRecord<String, String, String> record : userLogList)
        {
            var id = record.getId().getValue();
            var map = record.getValue();

            String timestamp = id.split("-")[0];
            LocalDateTime createdAt = Instant.ofEpochMilli(Long.parseLong(timestamp))
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime();

            var title = dealMap.get(Long.valueOf(map.get("dealId")));

            var userBehaviorLogDTO = UserBehaviorLogListDTO.UserBehaviorLogDTO.builder()
                    .logId(id)
                    .targetDealId(map.get("dealId"))
                    .targetDealTitle(title)
                    .type(map.get("type"))
                    .createdAt(createdAt.toString())
                    .build();

            responseList.add(userBehaviorLogDTO);
        }

        return new UserBehaviorLogListDTO(responseList);

    }

    public UserInterestListDTO getUserInterestWeight(Long userId)
    {
        var zSetOps = redisTemplate.opsForZSet();
        var set = zSetOps.reverseRangeWithScores("user:" + userId + ":interests", 0, 29);

        var tags = new ArrayList<UserInterestListDTO.UserInterestDTO>();
        for(var s : set){

            String member = s.getValue().toString();
            Double score = s.getScore();

            var name = hashTagRepository.findById(Long.valueOf(member)).map(HashTag::getName).orElse("name");

            var tag = UserInterestListDTO.UserInterestDTO.builder()
                    .tagName(name)
                    .weight(new BigDecimal(score).setScale(5, RoundingMode.HALF_UP))
                    .build();

            tags.add(tag);
        }

        return UserInterestListDTO.builder()
                .userInterestTags(tags)
                .userId(userId)
                .build();
    }

    public HashMap<String, Object> getTagsSimilarity(String name)
    {
        var response = new HashMap<String, Object>();
        var tags = new ArrayList<Map<String, Object>>();
        var list = hashTagRepository.findAllByName(name);
        log.info(list.toString());

        for(var tagId : list){
            var zSetOps = redisTemplate.opsForZSet();
            String key = "tag:similarity:" + tagId.toString();

            if(!redisTemplate.hasKey(key)) continue;

            var set = zSetOps.reverseRangeWithScores(key, 1, 15);
            var priceMap = redisTemplate.opsForHash().entries(key+":price");
            var titleMap = redisTemplate.opsForHash().entries(key+":title");

            var mainTag = new HashMap<String, Object>();
            var subTags = new ArrayList<HashMap<String, String>>();
            int rank = 0;
            for(var s : set){
                var tag = new HashMap<String, String>();
                String subTagId = s.getValue().toString();
                Double score = s.getScore();

                var price = priceMap.get(subTagId);
                var title = titleMap.get(subTagId);

                log.info(price.toString());

                var subTagName = hashTagRepository.findById(Long.valueOf(subTagId)).map(h -> h.getName()).orElse("subName");

                tag.put("rank", String.valueOf(rank));
                tag.put("tagName", subTagName);
                tag.put("similarityScore", String.valueOf(new BigDecimal(score).setScale(5, RoundingMode.HALF_UP)));
                tag.put("priceSimilarity", String.valueOf(new BigDecimal(String.valueOf(price)).setScale(5, RoundingMode.HALF_UP)));
                tag.put("titleSimilarity", String.valueOf(new BigDecimal(String.valueOf(title)).setScale(5, RoundingMode.HALF_UP)));
                tag.put("valuable", String.valueOf(score>0.5));

                rank++;

                subTags.add(tag);
            }

            var mainTagName = hashTagRepository.findById(Long.valueOf(tagId)).map(h -> h.getName()).orElse("subName");
            mainTag.put("tagName", mainTagName);
            mainTag.put("similarTags", subTags);
            tags.add(mainTag);
        }
        response.put("length", tags.size());
        response.put("tags", tags);
        return response;

    }

    public List<DealSearchResponseDTO> getInterestBoard(Long userId)
    {
        var key = "user:" + userId + ":interests";

        var response = new HashMap<String, Object>();

        // 관심 해쉬태그 불러오기
        var userHashTag = redisTemplate.opsForZSet().reverseRange(key,1, 10);
        var userHashTagIds = userHashTag.stream().map(u -> Long.valueOf(u.toString())).toList();//userHashTag.stream().map(t -> Integer.parseInt(t))

        // 각 관심해쉬태그에서 상품 두개씩 가져오기
        var deals = dealRepository.findDealsByTagId(userHashTagIds, 20);


        // 응답 매핑
        return deals.stream().map(deal -> {
            // 조회수는 증가 없이 조회만
            String viewKey = "deal:view:" + deal.getDealId();
            Object redisVal = redisTemplate.opsForValue().get(viewKey);
            long viewCount = redisVal == null ? 0L : ((Number) redisVal).longValue();

            long likeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.TRUE);
            long commentCount = commentRepository.countByDealId_DealIdAndIsDeleteFalse(deal.getDealId());

            Image firstImage = imageRepository
                    .findTopByRefIdAndImageTypeOrderByImageIndexAsc(deal.getDealId(), ImageType.DEAL)
                    .orElse(null);

            // 썸네일 가져오기
            ImageUrl imageUrl = null;
            if (firstImage != null) {
                imageUrl = new ImageUrl(
                        firstImage.getImageId(),
                        firstImage.getImageUrl(),
                        firstImage.getImageIndex()
                );
            }

            return new DealSearchResponseDTO(
                    deal.getDealId(),
                    imageUrl,
                    deal.getTitle(),
                    deal.getStoreId() != null ? deal.getStoreId().getName() : deal.getStoreName(),
                    DealService.getInfoTags(deal),
                    deal.getPrice(),
                    deal.getCreatedAt().toString(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();

    }


}
