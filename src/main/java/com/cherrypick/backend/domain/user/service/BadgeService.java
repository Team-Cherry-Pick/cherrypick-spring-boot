package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.domain.user.dto.response.UserResponseDTOs;
import com.cherrypick.backend.domain.user.entity.Badge;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.entity.UserBadge;
import com.cherrypick.backend.domain.user.repository.BadgeRepository;
import com.cherrypick.backend.domain.user.repository.UserBadgeRepository;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.enums.BadgeErrorCode;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cherrypick.backend.global.exception.BaseException;

@Service @RequiredArgsConstructor
public class BadgeService
{
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;

    /**
     * 유저에게 뱃지를 부여합니다.
     *
     * @param userId 뱃지를 부여할 유저 아이디입니다.
     * @param badgeId 부여할 뱃지의 아이디입니다.
     * @return BadgeRegisterDTO (success만 반환됨.)
     * @throws BaseException 404 BADGE_NOT_FIND, USER_NOT_FOUND
     */
    @Transactional
    public UserResponseDTOs.BadgeEquipDTO registerBadge(Long userId, Long badgeId)
    {
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        var badge = badgeRepository.findById(badgeId).orElseThrow(() -> new BaseException(BadgeErrorCode.BADGE_NOT_FIND));

        UserBadge userBadge = UserBadge.builder()
                    .user(user)
                    .badge(badge)
                    .build();

        userBadgeRepository.save(userBadge);

        return new UserResponseDTOs.BadgeEquipDTO(
                user.getUserId(),
                badge.getBadgeId(),
                badge.getDisplayName()
        );
    }

    /**
     * 유저가 뱃지를 착용합니다.
     *
     * @param userId 뱃지를 착용할 유저 아이디입니다.
     * @param badgeId 착용할 뱃지 아이디입니다.
     * @return BadgeEquipDTO (착용 유저 아이디, 착용 뱃지 아이디, 착용 뱃지명)
     * @throws BaseException 400 BADGE_NOT_OWNED
     * @throws BaseException 404 BADGE_NOT_FIND , USER_NOT_FOUND
     */
    @Transactional
    public UserResponseDTOs.BadgeEquipDTO equipBadge(Long userId, Long badgeId)
    {
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        var badge = badgeRepository.findById(badgeId).orElseThrow(() -> new BaseException(BadgeErrorCode.BADGE_NOT_FIND));

        if (!userBadgeRepository.existsByUserAndBadge(user, badge)){
            throw new BaseException(BadgeErrorCode.BADGE_NOT_OWNED);
        }

        user.setBadge(badge);
        userRepository.save(user);

        return new UserResponseDTOs.BadgeEquipDTO(
                user.getUserId(),
                badge.getBadgeId(),
                badge.getDisplayName()
        );
    }

}
