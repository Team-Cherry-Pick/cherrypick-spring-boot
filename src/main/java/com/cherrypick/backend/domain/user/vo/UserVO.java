package com.cherrypick.backend.domain.user.vo;

public record UserVO(
        Long userId,
        String userName,
        String userImageUrl,
        Long badgeId,
        String badgeName
) {}