package com.cherrypick.backend.domain.user.dto.response;

import lombok.Builder;

public class UserResponseDTOs {
    @Builder
    public record NicknameValidDTO(String nickname, boolean isValid, String details) {}
    public record DeleteResponseDTO(Long id, String message) {}
    public record BadgeRegisterDTO(String message) {}


}
