package com.cherrypick.backend.domain.user.vo;

public record User(
        Long userId,
        String userName,
        String userImageUrl
) {}