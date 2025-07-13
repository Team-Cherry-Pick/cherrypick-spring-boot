package com.cherrypick.backend.domain.auth.presentation.dto;

import lombok.Builder;

@Builder
public record OAuth2LoginSuccessResponseDTO(
        Long userId,
        String accessToken,
        String redirectURL,
        boolean isNewUser
) {
}
