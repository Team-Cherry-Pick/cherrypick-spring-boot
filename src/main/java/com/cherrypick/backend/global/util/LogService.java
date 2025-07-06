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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Slf4j @Component @RequiredArgsConstructor
public class LogService {


    private final ObjectMapper mapper;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${version:unknown}")
    private String version;

    @PostConstruct
    public void init() {

        mdcInitialize();
        MDC.put("logType", "SERVER_START_LOG");

        HashMap<String, String> map = new HashMap<>();
        map.put("start_msg", "Starting Server Application");
        log.info(toJson(map));

        MDC.remove("logType");
    }

    public void requestLog(long durationTime, String uriPattern, Long userId, String method, String clientIp, String queryString) {

        mdcInitialize();
        MDC.put("logType", "REQUEST_FILTER_LOG");

        HashMap<String, String> map = new HashMap<>();
        map.put("rqst_duration", String.valueOf(durationTime));
        map.put("rqst_uriPattern", Optional.ofNullable(uriPattern).orElse("unknown"));
        map.put("rqst_userId", String.valueOf(userId));
        map.put("rqst_method", Optional.ofNullable(method).orElse("unknown"));
        map.put("rqst_clientIp", Optional.ofNullable(clientIp).orElse("unknown"));
        map.put("rqst_queryString", Optional.ofNullable(queryString).orElse("unknown"));

        log.info(toJson(map));
        MDC.remove("logType");

    }

    public void errorLog(HttpStatus status, String msg) {
        mdcInitialize();
        MDC.put("logType", "ERROR_LOG");

        HashMap<String, String> map = new HashMap<>();
        map.put("error_msg", msg);
        map.put("error_status", String.valueOf(status));

        log.error(toJson(map));
        MDC.remove("logType");

    }

    private String toJson(HashMap<String, String> map) {

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
