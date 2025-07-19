package com.cherrypick.backend.domain.user.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN, CLIENT;

    @Override
    public String getAuthority() {
        return "ROLE" + this.name();
    }
}
