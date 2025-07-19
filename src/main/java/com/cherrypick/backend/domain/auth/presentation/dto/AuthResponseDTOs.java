package com.cherrypick.backend.domain.auth.presentation.dto;

import lombok.Builder;

public class AuthResponseDTOs {

    public record LogoutResponseDTO(
      Boolean isSuccess
    ){}

    @Builder
    // 엑세스 토큰 재발급 시 사용되는 DTO
    public record AccessToken(
            String accessToken
    ) {}

    @Builder
    public record TokenResponse(
            String accessToken,
            String refreshTokenCookie
    ){}


    @Builder
    // 내부적으로 사용하는 레지스터 토큰 DTO
    public record RegisterTokenDTO(
            String oauthId,
            String provider,
            UserEnvDTO userEnv
    ) {}

}
