package com.cherrypick.backend.domain.deal.domain.service.search;

import com.cherrypick.backend.domain.deal.domain.entity.QDeal;
import com.cherrypick.backend.domain.deal.domain.enums.SortType;
import com.querydsl.core.types.OrderSpecifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 딜 검색 결과 정렬을 처리하는 서비스
 */
@Service
public class DealSortService {

    /**
     * SortType을 QueryDSL OrderSpecifier로 변환
     *
     * @param sortType 정렬 타입
     * @return QueryDSL OrderSpecifier 리스트
     */
    public List<OrderSpecifier<?>> createOrderSpecifiers(SortType sortType) {
        if (sortType == null) {
            sortType = SortType.LATEST; // 기본값: 최신순
        }

        QDeal deal = QDeal.deal;
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        switch (sortType) {
            case LATEST -> orders.add(deal.createdAt.desc());
            case PRICE_HIGH -> orders.add(deal.price.discountedPrice.desc());
            case PRICE_LOW -> orders.add(deal.price.discountedPrice.asc());
            case POPULARITY -> orders.add(deal.heat.desc());
            case VIEWS -> orders.add(deal.totalViews.desc());
            case DISCOUNT_RATE -> {
                // 할인율 계산: (정가 - 할인가) / 정가 * 100
                // regularPrice가 0인 경우 제외 필요
                var discountRate = deal.price.regularPrice
                        .subtract(deal.price.discountedPrice)
                        .divide(deal.price.regularPrice)
                        .multiply(100);
                orders.add(discountRate.desc());
            }
        }

        // 동일 값에 대한 보조 정렬: createdAt desc
        if (sortType != SortType.LATEST) {
            orders.add(deal.createdAt.desc());
        }

        return orders;
    }
}