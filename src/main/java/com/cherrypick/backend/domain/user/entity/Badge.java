package com.cherrypick.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Getter @AllArgsConstructor
@Builder
@Setter @RequiredArgsConstructor @ToString
@EntityListeners(AuditingEntityListener.class)
public class Badge
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long badgeId;
    private String name;
    private String displayName;
    private String description;

    private String resource;

    @OneToMany(mappedBy = "badge",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    List<UserBadge> userBadges;

}
