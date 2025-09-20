package com.cherrypick.backend.domain.user.repository;

import com.cherrypick.backend.domain.user.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long>
{

}
