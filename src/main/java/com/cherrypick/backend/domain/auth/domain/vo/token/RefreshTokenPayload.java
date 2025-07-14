package com.cherrypick.backend.domain.auth.domain.vo.token;

import lombok.Builder;

@Builder
public record RefreshTokenPayload (
        Long userId,
        String deviceId
)
{


}
