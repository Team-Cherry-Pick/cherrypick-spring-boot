package com.cherrypick.backend.domain.deal.application.dto.response;

import lombok.Builder;


@Builder
public record TagSimilarityListDTO(

) {

    @Builder
    public record TagSimilarityDTO(
            Integer rank,
            String tagName,
            String totalSimilarityScore,
            String priceSimilarityScore,
            String titleSimilarityScore,
            Boolean valuable
    ){

    };

}
