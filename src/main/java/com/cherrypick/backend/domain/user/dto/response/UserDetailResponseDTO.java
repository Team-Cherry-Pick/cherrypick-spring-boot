package com.cherrypick.backend.domain.user.dto.response;

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
        Gender gender,          // nullable
        String birthday,        // nullable
        String imageURL,        // nullable
        Long imageId,           // nullable
        Long badgeId
)
{
    public static UserDetailResponseDTO from(User user, Image image) {
        return UserDetailResponseDTO.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .gender(user.getGender())
                .birthday(Optional.ofNullable(user.getBirthday()).map(LocalDate::toString).orElse(null))
                .imageId(image.getImageId())
                .imageURL(image.getImageUrl())
                .badgeId(user.getBadge().getBadgeId())
                .build();
    }
}
