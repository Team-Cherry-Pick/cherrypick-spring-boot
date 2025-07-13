package com.cherrypick.backend.domain.auth.presentation.controller;

import com.cherrypick.backend.domain.auth.application.AuthService;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthRequestDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.AuthResponseDTOs;
import com.cherrypick.backend.domain.auth.presentation.dto.RegisterDTO;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Optional;

@RestController @Tag(name = "Auth", description = "유저 인증 로직")
@RequiredArgsConstructor @Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Operation(
            summary = "로그아웃(refreshToken 파기)",
            description = "refreshToken을 파기합니다. 이름이 refreshToken인 쿠키의 만료시간을 0으로 해서 심는 방식입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 토큰 파기 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDTOs.LogoutResponseDTO> logout(HttpServletRequest request, HttpServletResponse response)
    {
        // 리프레시 토큰도 파기 후 재생성해서 보내줌
        var dummyCookie = jwtUtil.createDummyRefreshCookie();
        response.addHeader("Set-Cookie", dummyCookie.toString());

        return ResponseEntity.ok(new AuthResponseDTOs.LogoutResponseDTO(true));
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
    public ResponseEntity<AuthResponseDTOs.AccessToken> auth(@RequestBody AuthRequestDTOs.DeviceIdDTO deviceIdDto, HttpServletRequest request, HttpServletResponse response)
    {
        String clientDeviceId = Optional.ofNullable(deviceIdDto.deviceId()).orElse("default");

        // 쿠키에서 refresh를 찾아낸다.
        var cookies = Arrays.stream(
                Optional.ofNullable(request.getCookies()).orElseThrow(() -> new BaseException(GlobalErrorCode.NO_COOKIES_TO_READ))
        ).toList();

        String refreshToken = cookies.stream()
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new BaseException(UserErrorCode.REFRESH_TOKEN_REQUIRED));

        // 토큰 검증과 발급이 선행.
        Long tokenUserId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
        String tokenDeviceId = jwtUtil.getDeviceIdFromRefreshToken(refreshToken);
        if(!tokenDeviceId.equals(clientDeviceId)) throw new BaseException(UserErrorCode.REFRESH_TOKEN_NOT_VALID);

        var accessToken = authService.refreshAccessToken(tokenUserId, tokenDeviceId, refreshToken);

        // 리프레시 토큰도 파기 후 재생성해서 보내줌
        var newRefreshToken = jwtUtil.createRefreshToken(tokenUserId, tokenDeviceId);
        response.addHeader("Set-Cookie", jwtUtil.createRefreshCookie(newRefreshToken).toString());
        authService.saveResfreshToken(tokenUserId, tokenDeviceId, newRefreshToken);

        return ResponseEntity.ok(accessToken);
    }

    @Operation(
            summary = "최종 회원가입 완료",
            description = "유저가 정보를 입력하면 해당 데이터를 저장하고, " +
                    "새로운 권한이 담긴 AccessToken을 재발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 최종 회원가입 성공 + JWT 토큰 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 유저입니다."),
            @ApiResponse(responseCode = "403", description = "이미 등록된 유저입니다."),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/register-completion")
    public ResponseEntity<AuthResponseDTOs.AccessToken> userInfo(@RequestBody RegisterDTO registerDTO, HttpServletRequest request, HttpServletResponse response) {

        registerDTO.validate();
        return ResponseEntity.ok(authService.userRegisterComplete(registerDTO, response));
    }


}
