package com.cherrypick.backend.global.log.domain;


import com.cherrypick.backend.global.log.domain.port.LogAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Component @RequiredArgsConstructor
public class LogService {


    private final LogAppender logAppender;
    @Value("${spring.profiles.active}")
    private String env;

    /**
     * 애플리케이션 시작 시 서버 시작 로그를 기록합니다.
     * <p>
     * Spring Bean 초기화 완료 후 자동으로 실행되며,
     * 서버 구동 시점을 추적하기 위한 로그를 생성합니다.
     * </p>
     */
    @PostConstruct
    public void init() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("start_msg", "Starting Server Application");
        logAppender.appendInfo("SERVER_START_LOG", map);

    }


    /**
     * OpenAI API 사용량 로그를 기록합니다.
     * <p>
     * GPT 모델 사용에 따른 토큰 소비량을 추적하여 API 사용 비용 모니터링 및 프롬프트 최적화에 활용됩니다.
     * </p>
     *
     * @param promptTokens 입력 프롬프트 토큰 수
     * @param completionTokens 응답 생성 토큰 수
     * @param totalTokens 총 사용 토큰 수 (promptTokens + completionTokens)
     */
    public void openAiLog(Integer promptTokens, Integer completionTokens, Integer totalTokens)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("prompt_tokens", Optional.ofNullable(promptTokens).orElse(-1) );
        map.put("completion_tokens", Optional.ofNullable(completionTokens).orElse(-1));
        map.put("total_tokens", Optional.ofNullable(totalTokens).orElse(-1));

        logAppender.appendInfo("OPENAI_LOG", map);
    }

}
