package com.cherrypick.backend.domain.user.dto;

import lombok.Builder;

public class UserResponseDTOs {
    @Builder
    public record NicknameValidDTO(String nickname, boolean isValid, String details) {}

    @Builder
    public record UpdateDTO(
            Boolean isSuccess,
        String nickname,
        String email,
        String imageURL
    ){

    }


}
