package com.cherrypick.backend.global.config;


import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .version("1.0.0")
                .title("AiSaju Culture API")
                .description("AiSaju Culture API");

        return new OpenAPI().info(info);
    }
}