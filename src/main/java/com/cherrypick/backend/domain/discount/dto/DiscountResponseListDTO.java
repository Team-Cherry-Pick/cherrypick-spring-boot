package com.cherrypick.backend.domain.discount.dto;

import com.cherrypick.backend.domain.discount.entity.Discount;
import lombok.Builder;

import java.util.List;

@Builder
public record DiscountResponseListDTO(
        List<DiscountDTO> discounts
)
{
    @Builder
    public record DiscountDTO(
            Long discountId,
            String name
    ){}

    public static DiscountDTO from(Discount discount)
    {
        return DiscountDTO.builder()
                .discountId(discount.getDiscountId())
                .name(discount.getName())
                .build();

    }
}
