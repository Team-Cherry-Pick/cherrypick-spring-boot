package com.cherrypick.backend.global.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
    public class SlackNotifier {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendErrorLog(Exception ex, HttpServletRequest request) {
        if (slackWebhookUrl == null || slackWebhookUrl.isEmpty()) {
            log.warn("슬랙 웹훅 URL이 설정되지 않았습니다.");
            return;
        }

        String errorMessage = String.format(
                "*🚨 500 INTERNAL SERVER ERROR 발생*\n" +
                        "• *HTTP Method:* %s\n" +
                        "• *요청 URL:* %s\n" +
                        "• *에러 메시지:* %s\n" +
                        "• *발생 시각:* %s\n",
                request.getMethod(),
                request.getRequestURL(), // 백엔드 로컬 에러 구분을 위해 URL 전체 경로 전송
                ex.getMessage(),
                LocalDateTime.now()
        );

        try {
            Map<String, String> payload = Map.of("text", errorMessage);
            restTemplate.postForObject(slackWebhookUrl, payload, String.class);
        } catch (Exception e) {
            log.error("슬랙 메시지 전송 실패: {}", e.getMessage());
        }
    }

}
