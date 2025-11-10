package com.cherrypick.backend.domain.test;

import com.cherrypick.backend.domain.auth.application.AuthService;
import com.cherrypick.backend.domain.auth.domain.vo.UserEnv;
import com.cherrypick.backend.domain.auth.infra.factory.RefreshCookieFactory;
import com.cherrypick.backend.domain.auth.infra.jwt.AccessTokenProvider;
import com.cherrypick.backend.domain.auth.infra.jwt.RefreshTokenProvider;
import com.cherrypick.backend.domain.auth.infra.store.RefreshTokenStore;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.comment.service.CommentService;
import com.cherrypick.backend.domain.deal.adapter.out.OpenAiAdapter;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @Profile({"local", "dev"})
@RequiredArgsConstructor @Slf4j
@RequestMapping("/api/test")
public class TestController
{
    private final CommentService commentService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AccessTokenProvider accessTokenProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final RefreshCookieFactory refreshCookieFactory;
    private final OpenAiAdapter openAiAdapter;

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
                function getClientInfo() {
                  const ua = navigator.userAgent;
                  let os = "Unknown";
                  let browser = "Unknown";
                  let version = "Unknown";
            
                  // OS 추출
                  if (/windows nt/i.test(ua)) os = "Windows";
                  else if (/macintosh|mac os x/i.test(ua)) os = "macOS";
                  else if (/android/i.test(ua)) os = "Android";
                  else if (/iphone|ipad|ipod/i.test(ua)) os = "iOS";
                  else if (/linux/i.test(ua)) os = "Linux";
            
                  // 브라우저 및 버전 추출
                  if (/chrome\\/(\\d+)/i.test(ua) && !/edg/i.test(ua)) {
                    browser = "Chrome";
                    version = ua.match(/chrome\\/([\\d.]+)/i)[1];
                  } else if (/safari/i.test(ua) && !/chrome/i.test(ua)) {
                    browser = "Safari";
                    version = ua.match(/version\\/([\\d.]+)/i)?.[1] || "Unknown";
                  } else if (/firefox/i.test(ua)) {
                    browser = "Firefox";
                    version = ua.match(/firefox\\/([\\d.]+)/i)[1];
                  } else if (/edg/i.test(ua)) {
                    browser = "Edge";
                    version = ua.match(/edg\\/([\\d.]+)/i)[1];
                  }
            
                  return { os, browser, version };
                }
            
                function redirectToKakao() {
                
                  const input = document.getElementById("redirectInput").value.trim();
                  const safeRedirect = input.startsWith("/") ? input : "/";
                  const encodedRedirect = encodeURIComponent(safeRedirect);
            
                  const { os, browser, version } = getClientInfo();
                  const encodedOs = encodeURIComponent(os);
                  const encodedBrowser = encodeURIComponent(browser);
                  const encodedVersion = encodeURIComponent(version);
            
                  var url =  `http://localhost:8080/oauth2/authorization/kakao`        +
                              `?redirect=${encodedRedirect}` +
                              `&os=${encodedOs}` +
                              `&browser=${encodedBrowser}` +
                              `&deviceId=local-cached-device-uuid` +
                              `&version=${encodedVersion}`;
            
                  window.location.href = url;
                  alert(url);
                }
              </script>
            
            </body>
            </html>
            """;

    @Operation(
            summary = "OAuth2.0 카카오 로그인 테스트를 위한 페이지. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "스웨거에서 사용하지 말고, /test 페이지를 URL창에 입력해 테스트를 해주십시오."
    )
    @GetMapping("/auth/login")
    public String index() {
        return HTML;
    }

    @PostMapping("/comment/dummy")
    public String createDummyComment(){
        commentService.dummyDataSetting();
        return "success";
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
    @PostMapping("/auth/authorization")
    public ResponseEntity<AuthResponseDTOs.AccessToken> generateTestToken(
            @Parameter(description = "유저번호") @RequestParam Long userId,
            @RequestParam(defaultValue = "test-device") String deviceId,
            HttpServletResponse response
    ) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        var accessToken = accessTokenProvider.createToken(userId, user.getRoleNames(), user.getNickname());
        var refreshToken = refreshTokenProvider.createToken(userId, deviceId);

        // 저장
        refreshTokenStore.initializeToken(userId, deviceId, refreshToken, new UserEnv(deviceId, "test-os", "test-browser", "test-version"));

        // 쿠키 세팅
        var refreshCookie = refreshCookieFactory.createRefreshCookie(refreshToken);
        response.addHeader("Set-Cookie", refreshCookie);

        return ResponseEntity.ok(new AuthResponseDTOs.AccessToken(accessToken));
    }

    @GetMapping("/auth/expiretime")
    @Operation(
            summary = "토큰 만료 시간을 반환. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "해당 엑세스 토큰의 만료시간을 반환"
    )
    public ResponseEntity<String>  parseToken(@RequestParam String token) {

       var str = accessTokenProvider.getExpriationTime(token).toString();
        return ResponseEntity.ok(str);
    }

    @Operation(
            summary = "OpenAI 테스트 매서드",
            description = "프롬프트 넣고 값 잘 나오나 확인"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "응답을 잘 받아왔습니다."),
            @ApiResponse(responseCode = "404", description = "찾을 수 없는 유저입니다. userId를 다시 한번 확인해주세요."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/openai")
    public ResponseEntity<String> testOpenAI(@Parameter(description = "프롬프트") @RequestParam String prompt)
    {
        String response = openAiAdapter.requestClassify(prompt).get();

        return ResponseEntity.ok(response);
    }

}
