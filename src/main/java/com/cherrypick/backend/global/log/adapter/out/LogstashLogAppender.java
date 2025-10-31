package com.cherrypick.backend.global.log.adapter.out;

import com.cherrypick.backend.global.log.domain.port.LogAppender;
import com.cherrypick.backend.global.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;

@Component @RequiredArgsConstructor @Slf4j
public class LogstashLogAppender implements LogAppender
{

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${version:unknown}")
    private String version;

    /// 로그 스태시와 연결되어 로그를 찍는 주체
    /// 로그가 늘어나면 ENUM으로 분리하여 키를 관리하는 것도 좋은 방법일 듯.
    /// 필드명 변경은 가급적 , **절대로** 변경하지 말 것
    /// null이 들어와도 대응할 수 있도록 로그 설계 필수.


    @Override
    public void appendInfo(String logType, HashMap<String, Object> message)
    {
        mdcInitialize();
        MDC.put("logType", logType);

        log.info(JsonUtil.toJson(message));
        MDC.remove("logType");
    }

    @Override
    public void appendError(String logType, HashMap<String, Object> message)
    {
        mdcInitialize();
        MDC.put("logType", logType);

        log.error(JsonUtil.toJson(message));
        MDC.remove("logType");
    }

    private void mdcInitialize()
    {
        MDC.clear();
        MDC.put("env", env);
        MDC.put("version", version);
    }
}
