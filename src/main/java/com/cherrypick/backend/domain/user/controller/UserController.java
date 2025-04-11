package com.cherrypick.backend.domain.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor @Slf4j
public class UserController {

    @GetMapping("/")
    public String index() {
        return "<a href=\"http://localhost:8080/oauth2/authorization/kakao\">카카오</a>";
    }

}
