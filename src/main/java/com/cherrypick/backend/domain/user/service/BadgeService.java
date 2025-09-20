package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.domain.user.dto.response.UserResponseDTOs;
import com.cherrypick.backend.domain.user.entity.Badge;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.entity.UserBadge;
import com.cherrypick.backend.domain.user.repository.UserBadgeRepository;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class BadgeService
{
    private final UserBadgeRepository userBadgeRepository;

    @Transactional
    public UserResponseDTOs.BadgeRegisterDTO registerBadge(Long badgeId)
    {
        var userId = AuthUtil.getUserDetail().userId();

        var user = User.builder()
                .userId(userId)
                .build();
        var badge = Badge.builder()
                .badgeId(badgeId)
                .build();

        UserBadge userBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .build();

        userBadgeRepository.save(userBadge);

        return new UserResponseDTOs.BadgeRegisterDTO("success");
    }


}
