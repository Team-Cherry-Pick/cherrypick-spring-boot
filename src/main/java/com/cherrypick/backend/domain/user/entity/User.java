package com.cherrypick.backend.domain.user.entity;

import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter @AllArgsConstructor @Builder
@Setter @RequiredArgsConstructor @ToString
@EntityListeners(AuditingEntityListener.class)
public class User {

    /// 확장 가능성
    /// 유저 등급제 ,로그인 매서드 , 선호 해시태그/카테고리
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String oauthId;           // 소셜로그인 업체에서 받은 ID
    private String nickname;          // 유저 닉네임
    private String email;             // 이메일 (
    private LocalDate birthday;       // 생일
    @Enumerated(EnumType.STRING)
    private Gender gender;            // 성별
    private String provider;          // 업체명 ex) kakao
    @Enumerated(EnumType.STRING)
    private UserStatus status;        // 소프트 딜리트 (삭제 상태면

    /**
     * 사용자 권한 목록 (다중 권한 지원)
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    private double userWeight = 0.8;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true)
    List<UserBadge> userBadges;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 카카오 로그인 OAuth2User를 엔티티로 변환
    public static User fromKakao(OAuth2User oauth2User) {

        var userAttr = oauth2User.getAttributes();

        return User.builder()
                .oauthId(userAttr.get("id").toString())
                .email(Optional.ofNullable((HashMap<String, String>)userAttr.get("properties")).map(p -> p.get("account_email")).get().toString())
                .birthday(null)         // 이후에 등록됨.
                .gender(null)           // 이후에 등록됨
                .provider("kakao")
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * 역할 이름 목록 반환
     * @return ["ADMIN", "CLIENT"] 형태
     */
    public Set<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

}
