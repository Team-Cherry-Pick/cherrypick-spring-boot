package com.cherrypick.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @AllArgsConstructor
@Builder
@Setter @RequiredArgsConstructor @ToString
@EntityListeners(AuditingEntityListener.class)
public class UserBadge
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false, nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", updatable = false, nullable = false)
    private Badge badge;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
