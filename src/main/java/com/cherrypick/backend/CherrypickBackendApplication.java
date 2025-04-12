package com.cherrypick.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class CherrypickBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CherrypickBackendApplication.class, args);
    }

}
