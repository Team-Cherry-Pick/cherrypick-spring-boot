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
        map.put("message", "Starting Server Application");
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
        map.put("promptTokens", Optional.ofNullable(promptTokens).orElse(-1) );
        map.put("completionTokens", Optional.ofNullable(completionTokens).orElse(-1));
        map.put("totalTokens", Optional.ofNullable(totalTokens).orElse(-1));

        logAppender.appendInfo("OPENAI_LOG", map);
    }

    /**
     * 웹 클라이언트에서 전송한 커스텀 로그를 기록합니다.
     * <p>
     * 프론트엔드에서 발생한 사용자 행동, 이벤트, 오류 등을 추적하기 위한 로그를 기록합니다.
     * 로그 데이터는 자유로운 형식의 Map으로 전달받아 유연한 로깅이 가능합니다.
     * </p>
     * <p>
     * <strong>주의:</strong> 민감정보(비밀번호, 토큰 등)는 클라이언트에서 제외하고 전송해야 합니다.
     * </p>
     *
     * @param logType 로그 타입 (예: "BUTTON_CLICK", "PAGE_VIEW", "ERROR")
     * @param map 로그 데이터 (키-값 쌍의 자유 형식)
     */
    public void webCustomLog(String logType, HashMap<String, Object> map)
    {

        logAppender.appendInfo(logType, map);
    }

}
