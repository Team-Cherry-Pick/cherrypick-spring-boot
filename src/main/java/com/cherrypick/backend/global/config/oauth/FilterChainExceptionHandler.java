package com.cherrypick.backend.global.config.oauth;

import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

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

        // 401 에러에 대해 다루는 클래스임.
        UserErrorCode error = UserErrorCode.SECURITY_AUTHENTICATION_REQUIRED;

        response.setStatus(error.getStatus().value()); // 401
        response.setContentType("application/json;charset=UTF-8");

        // 응답 바디를 ResponseEntity와 비슷한 형태로 작성
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", error.getStatus().value(),
                        "error", error.getStatus().name(),
                        "message", error.getMessage(),
                        "path", request.getRequestURI()
                )
        );

        response.getWriter().print(body);
    }
}
