package com.cherrypick.backend.domain.oauth.dto;

import lombok.Builder;

public class AuthResponseDTOs {

    @Builder
    // 엑세스 토큰 재발급 시 사용되는 DTO
    public record AccessToken(
            String accessToken
    ) {}

    @Builder
    // 내부적으로 사용하는 레지스터 토큰 DTO
    public record RegisterTokenDTO(
            String oauthId,
            String provider,
            String userEnv // UserEnvDTO 를 JSON으로 직렬화한 필드
    ) {}

}
