package com.cherrypick.backend.global.config;

import com.cherrypick.backend.global.config.security.CustomAuthorizationRequestResolver;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.global.config.security.OAuth2SuccessHandler;
import com.cherrypick.backend.global.config.security.*;
import com.cherrypick.backend.global.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/user/test/jwt-filter").authenticated()
                        .requestMatchers("/").permitAll()
                        .anyRequest().permitAll())
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

        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        
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