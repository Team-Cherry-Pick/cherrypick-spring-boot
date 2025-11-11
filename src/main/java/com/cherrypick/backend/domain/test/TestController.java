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

    @Operation(
            summary = "런타임 에러 발생 테스트. ** 실 서비스에서는 사용하지 않습니다. **",
            description = "에러 처리 로직을 테스트하기 위해 의도적으로 런타임 에러를 발생시킵니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 오류 (의도된 에러)")
    })
    @GetMapping("/error/runtime")
    public ResponseEntity<String> throwRuntimeError(
            @Parameter(description = "에러 타입: npe(NullPointerException), divide(ArithmeticException), index(ArrayIndexOutOfBoundsException), illegal(IllegalArgumentException), runtime(RuntimeException)")
            @RequestParam(defaultValue = "runtime") String type
    ) {
        log.warn("🔥 의도적인 런타임 에러 발생 요청 - type: {}", type);

        switch (type.toLowerCase()) {
            case "npe":
                String nullStr = null;
                return ResponseEntity.ok(nullStr.length() + ""); // NullPointerException

            case "divide":
                int result = 100 / 0; // ArithmeticException
                return ResponseEntity.ok(String.valueOf(result));

            case "index":
                int[] arr = {1, 2, 3};
                return ResponseEntity.ok(String.valueOf(arr[10])); // ArrayIndexOutOfBoundsException

            case "illegal":
                throw new IllegalArgumentException("의도적으로 발생시킨 IllegalArgumentException 입니다.");

            case "runtime":
            default:
                throw new RuntimeException("의도적으로 발생시킨 RuntimeException 입니다. 에러 처리 테스트용입니다.");
        }
    }


}
