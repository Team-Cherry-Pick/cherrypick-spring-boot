package com.cherrypick.backend.domain.deal.dto.request;

import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.deal.vo.Shipping;
import com.cherrypick.backend.domain.image.vo.ImageUrl;

import java.util.List;

public record DealUpdateRequestDTO(

        Long dealId,
        List<Long> imageIds,
        String title,
        Long categoryId,
        String originalUrl,
        Long storeId,
        String storeName, // storeId가 null일 때만 사용
        Price price,
        Shipping shipping,
        String content,
        List<Long> discountIds,
        List<String> discountNames,
        String discountDescription,
        boolean isSoldOut

) {
}
