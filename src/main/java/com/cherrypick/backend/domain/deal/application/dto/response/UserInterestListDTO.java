package com.cherrypick.backend.domain.deal.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record UserInterestListDTO(

        Long userId,
        List<UserInterestDTO> userInterestTags

)
{
    @Builder
    public record UserInterestDTO(
            String tagName,
            BigDecimal weight
    ){

    }

}
