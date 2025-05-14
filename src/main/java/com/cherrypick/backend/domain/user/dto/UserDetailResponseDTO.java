package com.cherrypick.backend.domain.user.dto;

import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Gender;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Optional;

@Builder
public record UserDetailResponseDTO(
        Long userId,
        String nickname,
        String email,
        String phoneNumber,     // nullable
        Gender gender,          // nullable
        String birthday,        // nullable
        String imageURL,        // nullable
        Long imageId            // nullable
)
{
    public static UserDetailResponseDTO from(User user, Image image) {
        return UserDetailResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .birthday(Optional.ofNullable(user.getBirthday()).map(LocalDate::toString).orElse(null))
                .imageId(image.getImageId())
                .imageURL(image.getImageUrl())
                .build();
    }
}
