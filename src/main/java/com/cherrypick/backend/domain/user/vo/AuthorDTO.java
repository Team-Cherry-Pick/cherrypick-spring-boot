package com.cherrypick.backend.domain.user.vo;

import com.cherrypick.backend.domain.user.entity.Badge;
import com.cherrypick.backend.domain.user.entity.User;

/**
 * 작성자 정보 DTO
 * - 상세보기 및 목록에서 작성자 정보 표시에 사용
 *
 * @param userId 사용자 ID
 * @param userName 사용자 닉네임
 * @param userImageUrl 사용자 프로필 이미지 URL (null 가능)
 * @param badgeId 배지 ID
 * @param badgeName 배지 표시명
 */
public record AuthorDTO(
        Long userId,
        String userName,
        String userImageUrl,
        Long badgeId,
        String badgeName
) {

    /**
     * User 엔티티로부터 AuthorDTO 생성
     *
     * @param user 사용자 엔티티
     * @param imageUrl 프로필 이미지 URL (null 가능)
     * @return 생성된 DealAuthorDTO
     */
    public static AuthorDTO from(User user, String imageUrl)
    {
        Badge badge = user.getBadge();

        return new AuthorDTO(
                user.getUserId(),
                user.getNickname(),
                imageUrl,
                badge.getBadgeId(),
                badge.getDisplayName()
        );
    }

}