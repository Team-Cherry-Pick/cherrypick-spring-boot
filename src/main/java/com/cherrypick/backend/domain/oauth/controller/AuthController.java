package com.cherrypick.backend.domain.oauth.controller;

import com.cherrypick.backend.domain.oauth.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController @Tag(name = "Auth", description = "인증 두과장")
@RequiredArgsConstructor @Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;

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
                      const url = `https://api.repik.kr/oauth2/authorization/kakao?redirect=${encoded}`;

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
    @PostMapping("/test/authorization")
    public String accessToken(@Parameter(description = "유저번호") Long userId, HttpServletRequest request, HttpServletResponse response) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 리프레시 토큰도 파기 후 재생성해서 보내줌
        var newRefreshToken = jwtUtil.createRefreshToken(userId);
        response.addHeader("Set-Cookie", jwtUtil.createRefreshCookie(newRefreshToken).toString());
        authService.saveResfreshToken(userId, newRefreshToken);

        return jwtUtil.createAccessToken(user.getUserId(), user.getRole(), user.getNickname());
    }

    @Operation(
            summary = "access token 재발급",
            description = "refresh 쿠키를 가진 채로 실행해주시면 access token을 재발급 합니다." +
                    "이때 refresh token도 함께 재발급 되어 쿠키로 저장해드립니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 토큰 생성 성공"),
            @ApiResponse(responseCode = "404", description = "리프레시 토큰 / 유저를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTOs.AccessToken> auth(HttpServletRequest request, HttpServletResponse response)
    {
        // 쿠키에서 refresh를 찾아낸다.
        var cookies = Arrays.stream(request.getCookies()).toList();
        String refreshToken = cookies.stream()
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 토큰 검증과 발급이 선행.
        Long userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
        var accessToken = authService.refreshAccessToken(userId, refreshToken);

        // 리프레시 토큰도 파기 후 재생성해서 보내줌
        var newRefreshToken = jwtUtil.createRefreshToken(userId);
        response.addHeader("Set-Cookie", jwtUtil.createRefreshCookie(newRefreshToken).toString());
        authService.saveResfreshToken(userId, newRefreshToken);

        return ResponseEntity.ok(accessToken);
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
        var userDetail = jwtUtil.getUserDetailDTOFromAccessToken(Authorization);

        return "JWT Authorized : " + userDetail.userId() + " " + userDetail.nickname() + " " + userDetail.role().toString();
    }

}
