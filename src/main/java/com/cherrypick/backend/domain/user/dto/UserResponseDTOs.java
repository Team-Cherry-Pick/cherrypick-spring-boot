package com.cherrypick.backend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;

public class UserResponseDTOs {
    @Builder
    public record nicknameValid(String nickname, boolean isValid, String details) {}

}
