package com.cherrypick.backend.domain.auth.presentation.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder @Slf4j
public record UserEnvDTO(
        String deviceId,
        String os,
        String browser,
        String version

) {
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
