package com.cherrypick.backend.global.config.security;

import com.cherrypick.backend.global.util.AuthUtil;
import com.cherrypick.backend.global.util.LogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component @RequiredArgsConstructor
public class RequestLogFilter extends OncePerRequestFilter {

    private final LogService logService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - start;

        Long userId = AuthUtil.isAuthenticated() ?  AuthUtil.getUserDetail().userId() : -1;
        String method = request.getMethod();
        String queryString = request.getQueryString();
        String uriPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String ipAddress = getClientIp(request);

        logService.requestLog(duration, uriPattern, userId, method, ipAddress, queryString);

    }

    public static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }

}