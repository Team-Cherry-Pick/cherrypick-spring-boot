package com.cherrypick.backend.domain.user.controller;

import com.cherrypick.backend.domain.user.entity.Role;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController @Tag(name = "User", description = "유저 로직을 다룹니다.")
@RequiredArgsConstructor @Slf4j
@RequestMapping("/api/user")
public class UserController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

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
                      const url = `http://localhost:8080/oauth2/authorization/kakao?redirect=${encoded}`;

                      window.location.href = url;
                    }
                  </script>

                </body>
                </html>
            """;

    @Operation(
            summary = "OAuth2.0 카카오 로그인 테스트를 위한 페이지. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "스웨거에서 사용하지 말고, /test 페이지를 URL창에 입력해 테스트를 해주십시오."
    )
    @GetMapping("/login")
    public String index() {
        return HTML;
    }

    @Operation(
            summary = "테스트를 위한 JWT 생성 API. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "userId를 넣어 해당 유저의 엑세스 토큰을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 토큰 생성 성공"),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 유저입니다. userId를 다시 한번 확인해주세요."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/test/access-token")
    public String accessToken(@Parameter(description = "유저번호") Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        return jwtUtil.createAccessToken(user.getUserId(), user.getRole(), user.getNickname());
    }

    @Operation(
            summary = "JWT 필터의 검증을 위한 API. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "헤더에 JWT를 넣으면 유저 정보를 뽑아줍니다. 토큰이 없으면 접근이 불가합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 토큰 검증 성공"),
            @ApiResponse(responseCode = "401", description = "인증하지 않으면 스프링 시큐리티 FilterChain에서 걸립니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/test/jwt-filter")
    public String jwtFilter(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String Authorization) {

        log.info(Authorization);
        var userDetail = jwtUtil.getUserDetailDTO(Authorization);

        return "JWT Authorized : " + userDetail.userId() + " " + userDetail.nickname() + " " + userDetail.role().toString();
    }

    @GetMapping("/test")
    public String test() {

        return "SUCCESS";
    }

}
