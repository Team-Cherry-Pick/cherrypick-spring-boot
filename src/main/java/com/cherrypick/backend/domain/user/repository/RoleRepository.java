package com.cherrypick.backend.domain.user.repository;

import com.cherrypick.backend.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * 권한 이름으로 Role 조회
     * @param name 권한 이름 (예: "ADMIN", "CLIENT")
     * @return Role 엔티티
     */
    Optional<Role> findByName(String name);

}
