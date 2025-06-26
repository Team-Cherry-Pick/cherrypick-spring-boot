package com.cherrypick.backend.domain.oauth.dto;

import lombok.Builder;

public class AuthResponseDTOs {

    @Builder
    public record AccessToken(
            String accessToken
    ){}

}
