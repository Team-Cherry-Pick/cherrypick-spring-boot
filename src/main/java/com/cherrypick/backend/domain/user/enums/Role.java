package com.cherrypick.backend.domain.user.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ADMIN, CLIENT;

    @NotNull
    @Contract(pure = true)
    @Override
    public String getAuthority() {
        return "ROLE" + this.name();
    }
}
