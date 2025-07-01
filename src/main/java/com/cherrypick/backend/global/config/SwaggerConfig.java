package com.cherrypick.backend.global.config;


import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        // Security Scheme 정의
        SecurityScheme securityScheme;
        securityScheme = new SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name("Authorization");

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

        Info info = new Info()
                .version("1.0.0")
                .title("Repik API")
                .description("Repik API");

        Server server = new Server()
                .url("https://api.repik.kr")
                .description("Production Server");

        return new OpenAPI().info(info)
                .addSecurityItem(securityRequirement)  // Security Requirement 추가
                .schemaRequirement("BearerAuth", securityScheme) // Security Scheme 추가
                .servers(List.of(server));
    }
}