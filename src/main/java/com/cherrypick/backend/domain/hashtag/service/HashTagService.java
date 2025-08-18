package com.cherrypick.backend.domain.hashtag.service;

import com.cherrypick.backend.domain.hashtag.entity.DealTag;
import com.cherrypick.backend.domain.hashtag.entity.HashTag;
import com.cherrypick.backend.domain.hashtag.repository.DealTagRepository;
import com.cherrypick.backend.domain.hashtag.repository.HashTagRepository;
import com.cherrypick.backend.global.util.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Slf4j
public class HashTagService
{
    private final HashTagRepository hashTagRepository;
    private final DealTagRepository dealTagRepository;
    private final RedisTemplate<String, Object> restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LogService logService;


    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String openAiApiUrl;

    public void saveHashTags(Long targetDealId, Set<String> tagSet)
    {
        var dealTagList = new ArrayList<DealTag>();
        System.out.println("::::: 새로운 글 감지");
        for (String hashTag : tagSet) {

            var tagOp = hashTagRepository.findByName(hashTag);
            HashTag tag = null;
            tag = tagOp.orElseGet(() -> hashTagRepository.save(HashTag.builder()
                    .name(hashTag)
                    .build()));

            DealTag dealTag = DealTag.builder()
                    .dealId(targetDealId)
                    .hashTagId(tag.getHashTagId())
                    .build();

            dealTagList.add(dealTag);

        }
        System.out.println("해시태그 연결 : " + tagSet);

        dealTagRepository.saveAll(dealTagList);

    }

    // OpenAI API 호출
    public Set<String> getChatGPTResponse(String title, String content) {

        RestTemplate restTemplate = new RestTemplate();


        String prompt = String.format("""
                {제목 : %s  내용 : %s} \n
                이 핫딜 정보를 보고 해시태그 10개 뽑아줘. 해시태그는 이 정보를 본 유저가 관심있어할만한 키워드 목록이야.
                해시태그는 전자기기, 가전제품, 패션, 뷰티/헬스, 홈/리빙, 식품, 스포츠/레저, 자동차, 책/문구, 취미/완구, 게임, 영화, 음악, 여행, 애완동물, 유아용품, 정원용품, 세차용품, 컴퓨터 부품, 사진/영상, 오피스 용품, 의료기기, DIY 용품, 시계, 쥬얼리, 음향기기, 스마트홈, 교육용품, 선물, 공구, 텔레비전, 스마트폰 액세서리, 인테리어 소품, 화장품
                중에 골라서 만들어줘.\n
                상품에 대한 정보만 취급하고 그 외적인건(무료배송, 특가 등) 취급하지 말아줘.\n
                형식 : {과일 식품 사과 유기농 건강식품 선물}
                문자열을 분리할때는 반드시 띄워쓰기를 사용해줘.
                """, title, content);
        System.out.println(prompt);
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "gpt-3.5-turbo");
        requestMap.put("max_tokens", 300);
        requestMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = "";
        try {
            requestBody = objectMapper.writeValueAsString(requestMap);
        } catch (JsonProcessingException e) {
            System.err.println("요청 본문 직렬화 오류: " + e.getMessage());
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + openAiApiKey);

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {

            org.springframework.http.ResponseEntity<String> responseEntity =
                    restTemplate.exchange(openAiApiUrl+"/v1/chat/completions", org.springframework.http.HttpMethod.POST, request, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                JsonNode rootNode = objectMapper.readTree(response);
                var rawText = rootNode.path("choices").get(0).path("message").path("content").asText();

                JsonNode usage = rootNode.path("usage");
                int promptTokens = usage.path("prompt_tokens").asInt();
                int completionTokens = usage.path("completion_tokens").asInt();
                int totalTokens = usage.path("total_tokens").asInt();

                logService.openAiLog(promptTokens, completionTokens, totalTokens);

// 결과 반환
                return Arrays.stream(rawText.replaceAll("[{}]", "").split(" "))
                        .collect(Collectors.toSet());

            } else {
                System.err.println("OpenAI 요청 실패: " + responseEntity.getStatusCode());
                return null;
            }
        } catch (IOException e) {
            System.err.println("OpenAI 호출 오류: " + e.getMessage());
            return null;
        }
    }

}
