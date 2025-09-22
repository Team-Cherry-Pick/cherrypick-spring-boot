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
    @Query(value = "SELECT EXISTS(SELECT 1 FROM user_badge WHERE user_id = :userId AND badge_id = :badgeId)", nativeQuery = true)
    boolean existsByUserAndBadge(@Param("userId") Long userId, @Param("badgeId") Long badgeId);
}
