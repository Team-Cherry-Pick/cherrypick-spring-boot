package com.cherrypick.backend.global.security.filterchain;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
@RequiredArgsConstructor @Component
public class FilterChainExceptionHandler implements AuthenticationEntryPoint {

    // 401 에러에 대해 다루는 클래스임.

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        HttpStatus status;
        BaseErrorCode error;

        // 매핑된 핸들러가 없으면 404
        Object handler = request.getAttribute("uri_pattern");
        if (handler == null) {
            status = GlobalErrorCode.RESOURCE_NOT_FOUND.getStatus();
            error = GlobalErrorCode.RESOURCE_NOT_FOUND;
        } else {
            status = UserErrorCode.SECURITY_ACCESS_DENIED_BY_FILTER_CHAIN.getStatus();
            error = UserErrorCode.SECURITY_ACCESS_DENIED_BY_FILTER_CHAIN;
        }

        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");

        String body = objectMapper.writeValueAsString(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", status.value(),
                        "error", status.name(),
                        "message", error.getMessage(),
                        "path", request.getRequestURI()
                )
        );

        response.getWriter().print(body);
    }
}