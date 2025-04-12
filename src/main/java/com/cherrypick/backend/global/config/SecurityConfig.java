package com.cherrypick.backend.global.config;

import com.cherrypick.backend.global.config.oauth.CustomAuthorizationRequestResolver;
import com.cherrypick.backend.global.config.oauth.JWTFilter;
import com.cherrypick.backend.global.config.oauth.OAuth2Service;
import com.cherrypick.backend.global.config.oauth.OAuth2SuccessHandler;
import com.cherrypick.backend.global.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2Service oauth2Service;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    private final JWTUtil jwtUtil;
    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .anyRequest().permitAll());

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
                        .userService(oauth2Service))
                        .successHandler(oauth2SuccessHandler)
                );

        // 로그아웃 경로
        http
                .logout( logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")

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
        config.setAllowedOriginPatterns(List.of("*")); // or List.of("http://localhost:3000")
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}
