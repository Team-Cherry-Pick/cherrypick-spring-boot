package com.cherrypick.backend.global.config.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

@Builder
public record OAuth2LoginSuccessResponseDTO(

        Long userId,
        String accessToken,
        String refreshToken,
        String redirectURL,
        boolean isNewUser

) {
    static private final ObjectMapper MAPPER = new ObjectMapper();
    public String toJson() throws JsonProcessingException {return MAPPER.writeValueAsString(this);}
}
