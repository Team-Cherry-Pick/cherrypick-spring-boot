package com.cherrypick.backend.domain.user.entity;

import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

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
    @Enumerated(EnumType.STRING)
    private Role role;

    private double userWeight = 0.8;

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
                .nickname(Optional.ofNullable((HashMap<String, String>)userAttr.get("properties")).map(p -> p.get("nickname")).get().toString())
                .email(Optional.ofNullable((HashMap<String, String>)userAttr.get("properties")).map(p -> p.get("account_email")).get().toString())
                .birthday(null)         // 이후에 등록됨.
                .gender(null)           // 이후에 등록됨
                .provider("kakao")
                .status(UserStatus.PENDING)
                .role(Role.CLIENT_PENDING)
                .build();
    }

}
