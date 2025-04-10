package com.cherrypick.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

@Entity
@Getter @AllArgsConstructor @Builder
@Setter @RequiredArgsConstructor @ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String oauthId;           // 소셜로그인 업체에서 받은 ID
    private String nickname;          // 유저 닉네임
    private String email;             // 이메일
    private LocalDate birthday;            // 생일
    private String gender;            // 성별
    private String provider;          // 업체명 ex) kakao
    @Enumerated(EnumType.STRING)
    private UserStatus status;        // 소프트 딜리트 (삭제 상태면
    @Enumerated(EnumType.STRING)
    private Role role;

    public static User fromKakao(OAuth2User oauth2User) {

        var userAttr = oauth2User.getAttributes();

        return User.builder()
                .oauthId(userAttr.get("id").toString())
                .nickname(Optional.ofNullable((HashMap<String, String>)userAttr.get("properties")).map(p -> p.get("nickname")).get().toString())
                .email("example@example.com")
                .birthday(LocalDate.of(1999, 12, 31))
                .provider("kakao")
                .gender("male")
                .status(UserStatus.ACTIVE)
                .role(Role.CLIENT)
                .build();
    }

}
