package com.cherrypick.backend.global.config;

import com.cherrypick.backend.global.config.security.CustomAuthorizationRequestResolver;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.global.config.security.OAuth2SuccessHandler;
import com.cherrypick.backend.global.config.security.filterchain.FilterChainExceptionHandler;
import com.cherrypick.backend.global.config.security.filterchain.JWTFilter;
import com.cherrypick.backend.global.config.security.filterchain.RequestLogFilter;
import com.cherrypick.backend.global.config.security.filterchain.UriPatterMatchingFilterChain;
import com.cherrypick.backend.global.util.JWTUtil;
import com.cherrypick.backend.global.util.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
@EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {

    private final AuthService oauthService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    private final JWTUtil jwtUtil;
    private final FilterChainExceptionHandler filterChainExceptionHandler;
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${spring.profiles.active}")
    private String springProfilesActive;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http, LogService logService) throws Exception {

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(    "/v3/api-docs/**",      // OpenAPI 문서 JSON
                                            "/swagger-ui/**",        // Swagger UI 리소스
                                            "/swagger-ui.html"       // Swagger UI 진입점
                        ).permitAll()
                        // 게시글 관련 일체
                        .requestMatchers("/api/search/deal").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/deal/recommend", "/api/deal/*",                                     // 게시글 추천 시스템 조회 요청
                                "/api/category", "/api/store", "/api/discount",                         // 관련 리소스 조회 요청
                                "/api/best-comment/*", "/api/comment/*"                                   // 댓글 조회 요청

                        ).permitAll()
                        .requestMatchers("/api/deal/**").authenticated()                                     // 게시글 생성/수정/삭제 및 투표 등은 권한이 있어야함.
                        .requestMatchers("/api/comment/*").authenticated()                                   // 댓글 생성 / 삭제는 인증된 유저만 가능

                        // 이미지
                        .requestMatchers("/api/image").permitAll()                                           // 이미지 게시는 누구나 가능
                        .requestMatchers(HttpMethod.DELETE,"/api/image/*").authenticated()                   // 이미지 삭제는 인증된 유저만 가능

                        // 유저
                        .requestMatchers(HttpMethod.GET,"/api/user").authenticated()                         // 인증 유저 정보 조회 부분은 인증 유저만 가능.
                        .requestMatchers(HttpMethod.GET, "/api/user/*").permitAll()                          // 닉네임 유효성 판단 , 타겟 유저 정보 조회는 모두 가능
                        .requestMatchers("/api/user").authenticated()                                        // 삭제 / 수정은 인증 유저만 가능.

                        // 인증
                        .requestMatchers(
                                "/login/oauth2/code/**", "/api/auth/register-completion").anonymous()      // 회원가입은 미인증 유저만 가능
                        .requestMatchers("/api/auth/refresh").permitAll()                                    // 액세스 토큰이 만료된 상태에서도 재발급은 받을 수 있어야함.
                        // 테스트 코드
                        .requestMatchers("/api/test/**").permitAll()
                        .anyRequest().denyAll()
                );

        http
                .exceptionHandling(ex -> ex.authenticationEntryPoint(filterChainExceptionHandler));

        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        http
                .formLogin(AbstractHttpConfigurer::disable);


        http.addFilterBefore(new RequestLogFilter(logService), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JWTFilter(jwtUtil), RequestLogFilter.class);
        http.addFilterBefore(new UriPatterMatchingFilterChain(requestMappingHandlerMapping), JWTFilter.class);
        
        // oauth2
	    http
                .oauth2Login((oauth2) -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(customAuthorizationRequestResolver))
                        .loginPage("/login")
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                        .userService(oauthService))
                        .successHandler(oauth2SuccessHandler)
                );

        http
                .logout(logout -> logout
                        .logoutUrl("/logout")  // 로그아웃 요청 URL
                        .deleteCookies("refreshToken")  // 쿠키 삭제
                );


        // 세션 stateless
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // 로컬 환경이 아니라면 https만 되도록.
        // AWS ALB는 요청을 통과시킬때 X-Forwarded-Proto 라는 헤더를 붙여서 줌.
        if(!springProfilesActive.equalsIgnoreCase("local"))
        {
            http
                    .requiresChannel(channel ->
                            channel
                                    .requestMatchers(new RequestMatcher() {
                                        @Override
                                        public boolean matches(HttpServletRequest request) {
                                            String proto = request.getHeader("X-Forwarded-Proto");
                                            return proto == null || proto.equals("http");
                                        }
                                    })
                                    .requiresSecure()
                    );
        }

        return http.build();
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://repik.kr",
                "https://www.repik.kr",
                "https://api.repik.kr",
                "http://localhost:3000",
                "http://localhost:8080"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}