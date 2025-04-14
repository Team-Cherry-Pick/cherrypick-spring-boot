package com.cherrypick.backend.domain.oauth2.dto;

import lombok.Builder;

@Builder
public record OAuth2LoginSuccessResponseDTO(
        Long userId,
        String accessToken,
        String refreshToken,
        String redirectURL,
        boolean isNewUser

) {
}
