package com.cherrypick.backend.domain.user.entity;

import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

@Entity
@Getter @AllArgsConstructor @Builder
@Setter @RequiredArgsConstructor @ToString
public class User {

    /// 확장 가능성
    /// 유저 등급제 ,로그인 매서드 , 선호 해시태그/카테고리


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String oauthId;           // 소셜로그인 업체에서 받은 ID
    private String nickname;          // 유저 닉네임
    @Column(unique = true)
    private String phoneNumber;       // 핸드폰 번호 (이걸로 1인 1계정 함)
    private String email;             // 이메일 (
    private LocalDate birthday;       // 생일
    private String gender;            // 성별
    private String provider;          // 업체명 ex) kakao
    @Enumerated(EnumType.STRING)
    private UserStatus status;        // 소프트 딜리트 (삭제 상태면
    @Enumerated(EnumType.STRING)
    private Role role;

    // 카카오 로그인 OAuth2User를 엔티티로 변환
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
