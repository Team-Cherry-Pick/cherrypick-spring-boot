package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.user.entity.User;
import lombok.Builder;

public class UserResponseDTOs {
    @Builder
    public record NicknameValidDTO(String nickname, boolean isValid, String details) {}

    @Builder
    public record UpdateDTO(
        Boolean isSuccess,
        Long userId,
        String nickname,
        String email
    ){
        public static UpdateDTO from(User user) {
            return UpdateDTO.builder()
                    .isSuccess(true)
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .build();
        }

    }


}
