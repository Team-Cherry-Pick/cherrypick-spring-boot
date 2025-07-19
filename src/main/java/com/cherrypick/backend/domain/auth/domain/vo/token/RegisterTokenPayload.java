package com.cherrypick.backend.domain.auth.domain.vo.token;


import com.cherrypick.backend.domain.auth.domain.vo.UserEnv;
import lombok.Builder;

@Builder
public record RegisterTokenPayload(
        String oauthId,
        String provider,
        UserEnv userEnv
) {


}
