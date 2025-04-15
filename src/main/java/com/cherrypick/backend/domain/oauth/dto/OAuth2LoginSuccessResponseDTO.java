package com.cherrypick.backend.domain.oauth.dto;

import lombok.Builder;

@Builder
public record OAuth2LoginSuccessResponseDTO(
        Long userId,
        String accessToken,
        String redirectURL,
        boolean isNewUser
) {
}
