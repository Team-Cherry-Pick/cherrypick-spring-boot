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

    String HTML = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <title>카카오 로그인</title>
                </head>
                <body>

                  <input type="text" id="redirectInput" placeholder="/mypage" />
                  <button onclick="redirectToKakao()">카카오 로그인</button>

                  <script>
                    function redirectToKakao() {
                      const input = document.getElementById("redirectInput").value.trim();

                      // 기본값이나 안전성 검증
                      const safeRedirect = input.startsWith("/") ? input : "/";

                      // 인코딩해서 redirect 파라미터에 붙임
                      const encoded = encodeURIComponent(safeRedirect);
                      const url = `http://repik.kr:8080/oauth2/authorization/kakao?redirect=${encoded}`;

                      window.location.href = url;
                    }
                  </script>

                </body>
                </html>
            """;


    @GetMapping("/")
    public String index() {
        return HTML;
    }

}
