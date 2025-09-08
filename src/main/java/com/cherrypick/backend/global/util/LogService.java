package com.cherrypick.backend.global.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Component @RequiredArgsConstructor
public class LogService {


    private final ObjectMapper mapper;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${version:unknown}")
    private String version;

    /// 로그 스태시와 연결되어 로그를 찍는 주체
    /// 로그가 늘어나면 ENUM으로 분리하여 키를 관리하는 것도 좋은 방법일 듯.
    /// 필드명 변경은 가급적 , **절대로** 변경하지 말 것
    /// null이 들어와도 대응할 수 있도록 로그 설계 필수.

    @PostConstruct
    public void init() {

        mdcInitialize();
        MDC.put("logType", "SERVER_START_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("start_msg", "Starting Server Application");
        log.info(toJson(map));

        MDC.remove("logType");
    }

    // 엑세스 로그
    public void accessLog(Long durationTime, String uriPattern, Long userId, String deviceId,  String method, String clientIp, String url, String queryString) {

        mdcInitialize();
        MDC.put("logType", "ACCESS_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("access_duration", Optional.ofNullable(durationTime).orElse(-1L));
        map.put("access_uriPattern", Optional.ofNullable(uriPattern).orElse("unknown"));
        map.put("access_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("access_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("access_method", Optional.ofNullable(method).orElse("unknown"));
        map.put("access_clientIp", Optional.ofNullable(clientIp).orElse("unknown"));
        map.put("access_queryString", Optional.ofNullable(queryString).orElse("unknown"));
        map.put("access_url", Optional.ofNullable(url).orElse("unknown"));

        log.info(toJson(map));
        MDC.remove("logType");

    }

    // 로그인 로그
    public void loginLog(Boolean isNewUser, String provider, Long userId, String deviceId, String os, String browser, String version)
    {
        mdcInitialize();
        MDC.put("logType", "LOGIN_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("login_isNewUser", Optional.ofNullable(isNewUser).orElse(false));
        map.put("login_provider", Optional.ofNullable(provider).orElse("unknown"));
        map.put("login_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("login_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("login_os", Optional.ofNullable(os).orElse("unknown"));
        map.put("login_browser", Optional.ofNullable(browser).orElse("unknown"));
        map.put("login_version", Optional.ofNullable(version).orElse("unknown"));

        log.info(toJson(map));
        MDC.remove("logType");

    }

    public void errorLog(HttpStatus status, String msg, StackTraceElement[] stackTrace) {

        mdcInitialize();
        MDC.put("logType", "ERROR_LOG");

        var stackTraceList = Arrays.asList(stackTrace);
        if(env.equals("prod")) stackTraceList = stackTraceList.subList(0, Math.min(10, stackTraceList.size()));
        if(env .equals("local")) Arrays.stream(stackTrace).forEach(System.out::println);

        var stackTraceString = stackTraceList.stream().map(StackTraceElement::toString).collect(Collectors.joining("\n")) ;

        HashMap<String, Object> map = new HashMap<>();
        map.put("error_msg", String.valueOf(msg));
        map.put("error_status", String.valueOf(status));
        map.put("error_trace", stackTraceString);

        log.error(toJson(map));
        MDC.remove("logType");

    }

    // 유저 삭제 로그
    public void userDeleteLog(Long userId, String name, String email, String oauthId, String message) {
        mdcInitialize();
        MDC.put("logType", "USER_DELETE_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("deluser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("deluser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("deluser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("deluser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("deluser_email", Optional.ofNullable(email).orElse("unknown"));
        log.info(toJson(map));

        MDC.remove("logType");
    }

    // 유저 등록 로그
    public void userRegisterLog(Long userId, String name, String email, String oauthId, String message) {
        mdcInitialize();
        MDC.put("logType", "USER_REGISTER_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("reguser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("reguser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("reguser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("reguser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("reguser_email", Optional.ofNullable(email).orElse("unknown"));
        log.info(toJson(map));

        MDC.remove("logType");
    }

    // 구매버튼 클릭 로그
    public void clickPurchaseLog(Long userId,
                                 String deviceId,
                                 Long dealId,
                                 String dealTitle,
                                 Long categoryId,
                                 String categoryName)
    {
        mdcInitialize();
        MDC.put("logType", "PURCHASE_CLICK_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("purchaseclick_userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("purchaseclick_deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("purchaseclick_dealId", Optional.ofNullable(dealId).orElse(-1L));
        map.put("purchaseclick_dealTitle", Optional.ofNullable(dealTitle).orElse("unknown"));
        map.put("purchaseclick_categoryId", Optional.ofNullable(categoryId).orElse(-1L));
        map.put("purchaseclick_categoryName", Optional.ofNullable(categoryName).orElse("unknown"));
        log.info(toJson(map));

        MDC.remove("logType");
    }

    public void openAiLog(Integer promptTokens, Integer completionTokens, Integer totalTokens)
    {
        mdcInitialize();
        MDC.put("logType", "OPENAI_LOG");

        HashMap<String, Object> map = new HashMap<>();
        map.put("prompt_tokens", Optional.ofNullable(promptTokens).orElse(-1) );
        map.put("completion_tokens", Optional.ofNullable(completionTokens).orElse(-1));
        map.put("total_tokens", Optional.ofNullable(totalTokens).orElse(-1));
        log.info(toJson(map));

        MDC.remove("logType");
    }

    private String toJson(HashMap<String, Object> map) {

        try{
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }

    private void mdcInitialize()
    {
        MDC.clear();
        MDC.put("env", env);
        MDC.put("version", version);
    }

}
