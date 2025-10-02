package com.cherrypick.backend.domain.user.repository;

import com.cherrypick.backend.domain.user.entity.Badge;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long>
{
    @Query("SELECT COUNT(ub) > 0 FROM UserBadge ub WHERE ub.user.userId = :userId AND ub.badge.badgeId = :badgeId")
    boolean existsByUserAndBadge(@Param("userId") Long userId, @Param("badgeId") Long badgeId);

    @Query(value = "SELECT COUNT(*) FROM user_badge ub Where badge_id=:badgeId", nativeQuery = true)
    Integer getBadgeOwnerCount(@Param("badgeId") Long badgeId);

}
