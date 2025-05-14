package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserDetailResponseDTO(
        Long userId,
        String nickname,
        String email,
        String phoneNumber,
        String gender,
        String birthday,
        String imageURL,
        Long imageId
)
{
    public static UserDetailResponseDTO from(User user, Image image) {
        return UserDetailResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .imageId(image.getImageId())
                .imageURL(image.getImageUrl())

                .build();
    }
}
