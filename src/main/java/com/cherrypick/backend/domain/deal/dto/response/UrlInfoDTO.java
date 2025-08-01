package com.cherrypick.backend.domain.deal.dto.response;

import lombok.RequiredArgsConstructor;

public record UrlInfoDTO(
        long image,
        String title,
        int originPrice,
        int discountedPrice,
        long storeId,
        String storeName
) {

}
