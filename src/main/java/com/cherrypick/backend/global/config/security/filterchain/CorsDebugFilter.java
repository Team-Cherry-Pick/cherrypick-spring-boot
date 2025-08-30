package com.cherrypick.backend.global.config.security.filterchain;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CorsDebugFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        
        // CORS 관련 요청만 로깅
        if (origin != null || "OPTIONS".equals(method)) {
            System.out.println("=== CORS DEBUG ===");
            System.out.println("Method: " + method);
            System.out.println("URI: " + uri);
            System.out.println("Origin: " + origin);
            System.out.println("User-Agent: " + httpRequest.getHeader("User-Agent"));
            
            // OPTIONS 요청인 경우 추가 헤더들도 확인
            if ("OPTIONS".equals(method)) {
                System.out.println("Access-Control-Request-Method: " + httpRequest.getHeader("Access-Control-Request-Method"));
                System.out.println("Access-Control-Request-Headers: " + httpRequest.getHeader("Access-Control-Request-Headers"));
            }
        }
        
        chain.doFilter(request, response);
        
        // 응답 헤더도 확인
        if (origin != null || "OPTIONS".equals(method)) {
            System.out.println("=== CORS RESPONSE ===");
            System.out.println("Access-Control-Allow-Origin: " + httpResponse.getHeader("Access-Control-Allow-Origin"));
            System.out.println("Access-Control-Allow-Methods: " + httpResponse.getHeader("Access-Control-Allow-Methods"));
            System.out.println("Access-Control-Allow-Headers: " + httpResponse.getHeader("Access-Control-Allow-Headers"));
            System.out.println("Status: " + httpResponse.getStatus());
            System.out.println("==================");
        }
    }
}