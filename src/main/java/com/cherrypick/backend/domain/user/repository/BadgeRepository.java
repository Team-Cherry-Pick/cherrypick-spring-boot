package com.cherrypick.backend.domain.user.repository;

import com.cherrypick.backend.domain.user.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long>
{
    @Query(value = "SELECT * FROM badge where badge_id=:badgeId", nativeQuery = true)
    Optional<Badge> findBadgeById(@Param("badgeId") Long badgeId);
}
