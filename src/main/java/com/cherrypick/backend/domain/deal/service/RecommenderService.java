package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.UserBehaviorLogListDTO;
import com.cherrypick.backend.domain.deal.dto.response.UserInterestListDTO;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.hashtag.entity.HashTag;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.hashtag.repository.HashTagRepository;
import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.util.AuthUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Range;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Recommender", description = "추천 시스템")
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
        System.out.println("::::: 행동 로그 삽입 : " + behaviorDTO.toString());
        var rId = redisTemplate.opsForStream().add(STREAM_NAME, behaviorDTO.toMap());

        return rId.getValue();
    }

    // 모든 로그를 읽어와 유저의 것으로 정제 후 반환
    public UserBehaviorLogListDTO getLogByUserId(Long userId)
    {
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

    public DealSearchPageResponseDTO getInterestBoard(Long userId)
    {
        var key = "user:" + userId + ":interests";

        // 관심 해쉬태그 불러오기
        var userHashTag = redisTemplate.opsForZSet().reverseRange(key,0, 1);
        var userHashTag2 = redisTemplate.opsForZSet().reverseRange(key,2, 10);
        var userHashTagIds = userHashTag.stream().map(u -> Long.valueOf(u.toString())).toList();//userHashTag.stream().map(t -> Integer.parseInt(t))
        var userHashTagIds2 = userHashTag2.stream().map(u -> Long.valueOf(u.toString())).toList();//userHashTag.stream().map(t -> Integer.parseInt(t))

        // 각 관심해쉬태그에서 상품 두개씩 가져오기
        var deals = dealRepository.findDealsByTagId(userHashTagIds, 10);
        var deals2 = dealRepository.findDealsByTagId(userHashTagIds2, 10);

        System.out.println("::::: 유저가 관심 있어할만한 태그 :::::");
        for(var deal : deals){
            System.out.println(deal.getTitle());
        }
        System.out.println("\n\n::::: 유저가 관심 없을 수도 있는 태그 :::::");
        for(var deal : deals2){
            System.out.println(deal.getTitle());
        }

        deals.addAll(deals2);

        List<Long> dealIds = deals.stream().map(Deal::getDealId).toList();

        // Redis 조회수
        List<String> redisKeys = dealIds.stream().map(id -> "deal:view:" + id).toList();
        List<Object> redisResults = redisTemplate.opsForValue().multiGet(redisKeys);
        Map<Long, Long> viewCountMap = new HashMap<>();
        for (int i = 0; i < dealIds.size(); i++) {
            Object val = redisResults.get(i);
            long viewCount = 0L;
            if (val != null) {
                try {
                    viewCount = Long.parseLong(val.toString());
                } catch (NumberFormatException ignored) {}
            }
            viewCountMap.put(dealIds.get(i), viewCount);
        }

        // 좋아요 수
        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(dealIds, VoteType.TRUE);
        // 댓글 수
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(dealIds);
        // 이미지
        Map<Long, Image> imageMap = imageRepository.findTopImagesByDealIds(dealIds, ImageType.DEAL).stream()
                .collect(Collectors.toMap(Image::getRefId, img -> img, (a, b) -> a));

        List<DealSearchResponseDTO> responseList = deals.stream().map(deal -> {
            Long dealId = deal.getDealId();

            long viewCount = viewCountMap.getOrDefault(dealId, 0L);
            long likeCount = likeCountMap.getOrDefault(dealId, 0L);
            long commentCount = commentCountMap.getOrDefault(dealId, 0L);

            Image firstImage = imageMap.get(dealId);
            ImageUrl imageUrl = null;
            if (firstImage != null) {
                imageUrl = new ImageUrl(
                        firstImage.getImageId(),
                        firstImage.getImageUrl(),
                        firstImage.getImageIndex()
                );
            }

            return new DealSearchResponseDTO(
                    dealId,
                    imageUrl,
                    deal.getTitle(),
                    deal.getStoreId() != null ? deal.getStoreId().getName() : deal.getStoreName(),
                    DealService.getInfoTags(deal),
                    deal.getPrice(),
                    deal.getUserId() != null ? deal.getUserId().getNickname() : null,
                    deal.getCreatedAt().toString(),
                    (int) deal.getHeat(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();

        return new DealSearchPageResponseDTO(responseList, false);

    }

    public DealSearchPageResponseDTO getInterestBoard()
    {
        var userId = AuthUtil.getUserDetail().userId();
        return getInterestBoard(userId);
    }

    public DealListResponse getAllList()
    {
        var response = new HashMap<String, Object>();
        PageRequest pageRequest = PageRequest.of(0, 40);
        var deals = dealRepository.findAll(pageRequest);

        List<Long> dealIds = deals.stream().map(Deal::getDealId).toList();

        // Redis 조회수
        List<String> redisKeys = dealIds.stream().map(id -> "deal:view:" + id).toList();
        List<Object> redisResults = redisTemplate.opsForValue().multiGet(redisKeys);
        Map<Long, Long> viewCountMap = new HashMap<>();
        for (int i = 0; i < dealIds.size(); i++) {
            Object val = redisResults.get(i);
            long viewCount = 0L;
            if (val != null) {
                try {
                    viewCount = Long.parseLong(val.toString());
                } catch (NumberFormatException ignored) {}
            }
            viewCountMap.put(dealIds.get(i), viewCount);
        }

        // 좋아요 수
        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(dealIds, VoteType.TRUE);
        // 댓글 수
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(dealIds);
        // 이미지
        Map<Long, Image> imageMap = imageRepository.findTopImagesByDealIds(dealIds, ImageType.DEAL).stream()
                .collect(Collectors.toMap(Image::getRefId, img -> img, (a, b) -> a));



        var responseList = deals.stream().map(deal -> {
            Long dealId = deal.getDealId();

            long viewCount = viewCountMap.getOrDefault(dealId, 0L);
            long likeCount = likeCountMap.getOrDefault(dealId, 0L);
            long commentCount = commentCountMap.getOrDefault(dealId, 0L);

            Image firstImage = imageMap.get(dealId);
            ImageUrl imageUrl = null;
            if (firstImage != null) {
                imageUrl = new ImageUrl(
                        firstImage.getImageId(),
                        firstImage.getImageUrl(),
                        firstImage.getImageIndex()
                );
            }

            return new DealListResponse.DealResponse(
                    dealId,
                    imageUrl,
                    deal.getTitle(),
                    deal.getStoreId() != null ? deal.getStoreId().getName() : deal.getStoreName(),
                    DealService.getInfoTags(deal),
                    deal.getPrice(),
                    deal.getUserId() != null ? deal.getUserId().getNickname() : null,
                    deal.getCreatedAt().toString(),
                    (int) deal.getHeat(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut(),
                    hashTagRepository.findAllByDealId(dealId)
            );
        }).toList();

        return new DealListResponse(responseList);
    }

    @Builder
    public record DealListResponse(
            List<DealResponse> deals
    ){
        @AllArgsConstructor @Getter
        public static class DealResponse extends DealSearchResponseDTO{
            List<String> tags;

            public DealResponse(Long dealId, ImageUrl imageUrl, String title, String s, List<String> infoTags, Price price, String nickname, String string, int heat, int likeCount, int commentCount, boolean soldOut, List<String> tags)
            {
                super(dealId, imageUrl, title, s, infoTags, price, nickname, string, heat, likeCount, commentCount, soldOut);
                this.tags = tags;
            }
        }
    }


}
