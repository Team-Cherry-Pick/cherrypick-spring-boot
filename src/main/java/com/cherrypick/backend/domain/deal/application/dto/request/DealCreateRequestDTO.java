package com.cherrypick.backend.domain.deal.application.dto.request;

import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceVO;
import com.cherrypick.backend.domain.deal.domain.entity.vo.ShippingVO;

import lombok.Builder;

import java.util.List;

@Builder
public record DealCreateRequestDTO(

        String title,
        Long categoryId,
        List<Long> imageIds,
        String originalUrl,
        Long storeId,
        String storeName, // storeId가 null일 때만 사용
        PriceVO price,
        ShippingVO shipping,
        String content,
        List<Long> discountIds,
        List<String> discountNames,
        String discountDescription
) {

}
