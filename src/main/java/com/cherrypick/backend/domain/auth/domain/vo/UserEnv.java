package com.cherrypick.backend.domain.auth.domain.vo;

import com.cherrypick.backend.domain.auth.presentation.dto.UserEnvDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

@Builder
public record UserEnv(
        String deviceId,
        String os,
        String browser,
        String version
)
{

    static private final ObjectMapper mapper = new ObjectMapper();

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static UserEnvDTO fromJson(String json) {
        try {
            return mapper.readValue(json, UserEnvDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
