package com.cherrypick.backend.domain.user.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN, CLIENT, CLIENT_PENDING;

    @Override
    public String getAuthority() {
        return "";
    }
}
