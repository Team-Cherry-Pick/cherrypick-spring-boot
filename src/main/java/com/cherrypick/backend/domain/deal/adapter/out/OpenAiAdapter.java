package com.cherrypick.backend.domain.deal.adapter.out;

import com.cherrypick.backend.global.util.LogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component @RequiredArgsConstructor
public class OpenAiAdapter {

    /// 요청과 응답만 해주는 클래스
    /// 프롬프트를 입력하고 응답으로 온 스트링도 직접 파싱해야함.

    @Value("${openai.api-key}")
    private String openAiApiKey;
    @Value("${openai.api-url}")
    private String openAiApiUrl;

    private final LogService logService;

    public Optional<String> requestClassify(String prompt) {

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "gpt-3.5-turbo");
        requestMap.put("max_tokens", 300);
        requestMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestMap.put("temperature", 0.0);

        return Optional.ofNullable(request(requestMap));
    }

    public Optional<String> requestContent(String prompt) {

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model", "gpt-4o");
        requestMap.put("max_tokens", 500);
        requestMap.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        requestMap.put("temperature", 1.0);

        return Optional.ofNullable(request(requestMap));
    }


    private String request(Map<String, Object> requestMap) {

        RestTemplate restTemplate = new RestTemplate();
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
                    restTemplate.exchange(openAiApiUrl, org.springframework.http.HttpMethod.POST, request, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                String response = responseEntity.getBody();
                JsonNode rootNode = objectMapper.readTree(response);
                var rawText = rootNode.path("choices").get(0).path("message").path("content").asText();

                JsonNode usage = rootNode.path("usage");
                int promptTokens = usage.path("prompt_tokens").asInt();
                int completionTokens = usage.path("completion_tokens").asInt();
                int totalTokens = usage.path("total_tokens").asInt();

                logService.openAiLog(promptTokens, completionTokens, totalTokens);

                return rawText;

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
