package com.cherrypick.backend.domain.user.repository;

import com.cherrypick.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    @Query(value="SELECT * FROM user WHERE oauth_id=:id", nativeQuery = true)
    Optional<User> findUserByOauthId(@Param("id") String oauthId);
}
