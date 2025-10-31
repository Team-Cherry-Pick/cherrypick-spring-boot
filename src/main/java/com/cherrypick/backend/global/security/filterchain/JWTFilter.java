package com.cherrypick.backend.global.security.filterchain;

import com.cherrypick.backend.domain.auth.infra.jwt.AccessTokenProvider;
import com.cherrypick.backend.global.util.JwtUtil;
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

    private final AccessTokenProvider accessTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // [0] Authorization 헤더 찾음.
        String authorization = request.getHeader("Authorization");

        // [1] Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // [2] Bearer 부분 제거 후 순수 토큰만 획득.
        String token = JwtUtil.removeBearer(authorization);

        // [3] 토큰이 만료되었는지 검증
        if (accessTokenProvider.isExpired(token)) {

            // [4-1] 유효하지 않은 토큰인 경우 유저 정보를 등록하지 않고 다음 필터로 넘어감.
            filterChain.doFilter(request, response);
        }
        else{
            // [4-1] 유효할 경우 유저 정보를 등록 후 다음 필터로 넘어감
            var authenticatedUser = accessTokenProvider.getAuthenticatedUser(token);
            Authentication authToken = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);
        }

    }

}