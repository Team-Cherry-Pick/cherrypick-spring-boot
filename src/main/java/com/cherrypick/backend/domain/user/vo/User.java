package com.cherrypick.backend.domain.user.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public class User {

    private Long userId;
    private String userName;
    private String userImageUrl;
}
