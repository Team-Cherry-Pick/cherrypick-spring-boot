package com.cherrypick.backend.domain.deal.util;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.enums.ShippingType;
import com.cherrypick.backend.domain.discount.entity.Discount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoTagGenerator
{
    // 인포 태그 생성 메소드
    public static List<String> getInfoTags(Deal deal) {
        List<String> infoTags = new ArrayList<>();

        // 배송 타입이 FREE이면 #무료배송 추가
        if (deal.getShipping() != null && deal.getShipping().getShippingType() == ShippingType.FREE) {
            infoTags.add("무료배송");
        }

        // 할인 ID가 있다면 해당 할인 ID의 이름에 해시태그 추가
        if (deal.getDiscounts() != null && !deal.getDiscounts().isEmpty()) {
            for (Discount discount : deal.getDiscounts()) {
                infoTags.add(discount.getName());
            }
        }

        // 할인 이름이 있다면, 카드나 쿠폰 이름을 해시태그로 추가
        if (deal.getDiscountName() != null && !deal.getDiscountName().isEmpty()) {
            String[] discountNames = deal.getDiscountName().split(", ");
            infoTags.addAll(Arrays.asList(discountNames));
        }
        return infoTags;
    }
}
