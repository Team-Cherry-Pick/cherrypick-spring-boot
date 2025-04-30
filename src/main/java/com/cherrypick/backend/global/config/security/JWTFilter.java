package com.cherrypick.backend.global.config.security;

import com.cherrypick.backend.global.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@RequiredArgsConstructor @Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //request에서 Authorization 헤더를 찾음
        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.trace(":::: JWTFilter : Token NULL");
            filterChain.doFilter(request, response);

            return;
        }

        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = jwtUtil.removeBearer(authorization);

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {

            log.trace(":::: JWTFilter : Token Expired");
            filterChain.doFilter(request, response);

            return;
        }

        var userDetailDTO = jwtUtil.getUserDetailDTOFromAccessToken(token);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(userDetailDTO, null, userDetailDTO.getAuthorities());
        // 사용자 등록 완료.
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

}