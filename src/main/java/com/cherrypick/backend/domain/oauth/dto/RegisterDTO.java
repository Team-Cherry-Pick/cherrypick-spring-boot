package com.cherrypick.backend.domain.oauth.dto;

import com.cherrypick.backend.domain.user.dto.UserUpdateRequestDTO;
import lombok.Builder;

@Builder
public record RegisterDTO(
        String registerToken,
        UserUpdateRequestDTO updateDTO
) {
}
