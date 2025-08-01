package com.cherrypick.backend.domain.deal.dto.response;

public record UrlInfoDTO(
        long image,
        String title,
        int originPrice,
        int discountedPrice,
        long storeId,
        String storeName
) {
}
