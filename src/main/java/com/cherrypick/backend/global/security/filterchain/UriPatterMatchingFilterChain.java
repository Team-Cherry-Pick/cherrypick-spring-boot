package com.cherrypick.backend.global.security.filterchain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class UriPatterMatchingFilterChain extends OncePerRequestFilter {

    private final Set<String> knownPatterns;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public UriPatterMatchingFilterChain(RequestMappingHandlerMapping handlerMapping) {
        this.knownPatterns = handlerMapping.getHandlerMethods().keySet().stream()
                .map(RequestMappingInfo::getPatternValues) // 여기가 핵심
                .flatMap(Set::stream)
                .collect(Collectors.toSet());

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        knownPatterns.stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .findFirst()
                .ifPresent(pattern -> request.setAttribute("uri_pattern", pattern));

        filterChain.doFilter(request, response);
    }
}