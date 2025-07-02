package com.cherrypick.backend.domain.test;

import com.cherrypick.backend.domain.comment.controller.CommentController;
import com.cherrypick.backend.domain.comment.service.CommentService;
import com.cherrypick.backend.domain.deal.service.DealCrawlService;
import com.cherrypick.backend.domain.oauth.service.AuthService;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.user.service.UserService;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@RestController @Profile({"local", "dev"})
@RequiredArgsConstructor @Slf4j
@RequestMapping("/api/test")
public class TestController
{
    private final CommentService commentService;
    private final DealCrawlService dealCrawlService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;


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

    // 크롤링 API
    @GetMapping("/deal/crawl-board")
    public String crawlBoard(String count) {
        try {
            dealCrawlService.crawlAndSaveBoard(Integer.parseInt(count));
            return "게시글 크롤링 및 저장 완료";
        } catch (Exception e) {
            return "오류 발생: " + e.getMessage();
        }
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
    @GetMapping("/auth/jwt-filter")
    public String jwtFilter(@Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String Authorization) {

        log.info(Authorization);
        var userDetail = jwtUtil.getUserDetailDTOFromAccessToken(Authorization);

        return "JWT Authorized : " + userDetail.userId() + " " + userDetail.nickname() + " " + userDetail.role().toString();
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
    public String accessToken(@Parameter(description = "유저번호") Long userId, String deviceId, HttpServletRequest request, HttpServletResponse response) {

        User user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        // 리프레시 토큰도 파기 후 재생성해서 보내줌
        var newRefreshToken = jwtUtil.createRefreshToken(userId);
        response.addHeader("Set-Cookie", jwtUtil.createRefreshCookie(newRefreshToken).toString());
        authService.saveResfreshToken(userId, deviceId, newRefreshToken);

        return jwtUtil.createAccessToken(user.getUserId(), user.getRole(), user.getNickname());
    }
}
