package com.cherrypick.backend.global.config.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;

@Builder
public record OAuth2LoginSuccessResponseDTO(
        Long userId,
        String accessToken,
        String refreshToken,
        String redirectURL,
        boolean isNewUser

) {
}
