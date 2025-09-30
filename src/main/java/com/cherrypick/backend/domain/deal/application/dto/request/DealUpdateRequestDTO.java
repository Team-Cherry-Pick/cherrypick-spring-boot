package com.cherrypick.backend.domain.deal.application.dto.request;

import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceVO;
import com.cherrypick.backend.domain.deal.domain.entity.vo.ShippingVO;

import java.util.List;

public record DealUpdateRequestDTO(

        Long dealId,
        List<Long> imageIds,
        String title,
        Long categoryId,
        String originalUrl,
        Long storeId,
        String storeName, // storeId가 null일 때만 사용
        PriceVO price,
        ShippingVO shipping,
        String content,
        List<Long> discountIds,
        List<String> discountNames,
        String discountDescription,
        boolean isSoldOut

) {
}
