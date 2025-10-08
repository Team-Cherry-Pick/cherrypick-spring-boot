package com.cherrypick.backend.domain.deal.domain.service.search;

import com.cherrypick.backend.domain.deal.domain.service.CategoryService;
import com.cherrypick.backend.domain.comment.entity.QComment;
import com.cherrypick.backend.domain.deal.domain.entity.QDeal;
import com.cherrypick.backend.domain.deal.domain.entity.vo.Filter;
import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceFilter;
import com.cherrypick.backend.domain.deal.domain.enums.PriceType;
import com.cherrypick.backend.domain.deal.domain.enums.ShippingType;
import com.cherrypick.backend.domain.deal.domain.enums.TimeRangeType;
import com.cherrypick.backend.domain.vote.entity.QVote;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 검색 필터를 QueryDSL BooleanExpression으로 변환하는 서비스
 */
@Service
@RequiredArgsConstructor
public class DealFilterFactory {

    private final CategoryService categoryService;

    /**
     * 카테고리 필터 (하위 카테고리 포함)
     */
    public BooleanExpression createCategoryFilter(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        QDeal deal = QDeal.deal;
        List<Long> categoryList = categoryService.getCategoryWithChildren(categoryId);

        return deal.category.categoryId.in(categoryList);
    }

    /**
     * 키워드 필터 (제목 또는 내용에 포함)
     */
    public BooleanExpression createKeywordFilter(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        QDeal deal = QDeal.deal;
        return deal.title.containsIgnoreCase(keyword)
                .or(deal.content.containsIgnoreCase(keyword));
    }

    /**
     * 품절 상품 필터
     */
    public BooleanExpression createSoldOutFilter(Filter filters) {
        QDeal deal = QDeal.deal;
        boolean viewSoldOut = filters != null && filters.viewSoldOut();

        // viewSoldOut이 true면 품절 포함, false면 품절 제외
        return viewSoldOut ? null : deal.isSoldOut.isFalse();
    }

    /**
     * 무료배송 필터
     */
    public BooleanExpression createFreeShippingFilter(Filter filters) {
        QDeal deal = QDeal.deal;
        boolean freeShipping = filters != null && filters.freeShipping();

        return freeShipping ? deal.shipping.shippingType.eq(ShippingType.FREE) : null;
    }

    /**
     * 해외직구 필터
     */
    public BooleanExpression createGlobalShippingFilter(Filter filters) {
        QDeal deal = QDeal.deal;
        boolean globalShipping = filters != null && filters.globalShipping();

        return globalShipping ? deal.price.priceType.eq(PriceType.USD) : null;
    }

    /**
     * 날짜 범위 필터
     */
    public BooleanExpression createDateRangeFilter(TimeRangeType timeRange) {
        if (timeRange == null) {
            return null;
        }

        QDeal deal = QDeal.deal;
        LocalDateTime startDate = resolveStartDate(timeRange);
        LocalDateTime endDate = LocalDateTime.now();

        // JPQL과 동일한 방식: 각 조건을 독립적으로 처리
        BooleanExpression result = null;

        if (startDate != null) {
            result = deal.createdAt.goe(startDate);  // >= (greater or equal)
        }

        if (endDate != null) {
            BooleanExpression endCondition = deal.createdAt.loe(endDate);  // <= (less or equal)
            result = result == null ? endCondition : result.and(endCondition);
        }

        return result;
    }

    /**
     * 가격 필터
     *
     * 원본 JPQL 로직:
     * (priceType = 'VARIOUS' AND variousPrice = TRUE)
     * OR
     * (priceType IN priceTypes AND minPrice <= price <= maxPrice)
     *
     * @param priceFilter 가격 필터 (타입, 최소/최대 가격)
     * @param variousPrice VARIOUS 타입 포함 여부 (기본 true)
     */
    public BooleanExpression createPriceFilter(PriceFilter priceFilter, Boolean variousPrice) {
        QDeal deal = QDeal.deal;

        // 1. VARIOUS 조건 (variousPrice가 true일 때만)
        BooleanExpression variousCondition = null;
        boolean includeVariousPrice = variousPrice != null ? variousPrice : true;
        if (includeVariousPrice) {
            variousCondition = deal.price.priceType.eq(PriceType.VARIOUS);
        }

        // 2. PriceFilter 조건
        BooleanExpression priceFilterCondition = null;
        if (priceFilter != null) {
            PriceType type = priceFilter.priceType();
            Double minPrice = priceFilter.minPrice();
            Double maxPrice = priceFilter.maxPrice();

            List<BooleanExpression> priceConditions = new ArrayList<>();

            // 가격 타입 필터
            if (type != null) {
                priceConditions.add(deal.price.priceType.eq(type));
            }

            // 최소 가격
            if (minPrice != null) {
                priceConditions.add(deal.price.discountedPrice.goe(minPrice));
            }

            // 최대 가격
            if (maxPrice != null) {
                priceConditions.add(deal.price.discountedPrice.loe(maxPrice));
            }

            priceFilterCondition = combineAnd(priceConditions);
        }

        // 3. OR 결합
        if (variousCondition != null && priceFilterCondition != null) {
            return variousCondition.or(priceFilterCondition);
        } else if (variousCondition != null) {
            return variousCondition;
        } else {
            return priceFilterCondition;
        }
    }

    /**
     * 할인 방식 필터
     */
    public BooleanExpression createDiscountFilter(List<Long> discountIds) {
        if (discountIds == null || discountIds.isEmpty()) {
            return null;
        }

        QDeal deal = QDeal.deal;
        return deal.discounts.any().discountId.in(discountIds);
    }

    /**
     * 스토어 필터
     */
    public BooleanExpression createStoreFilter(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return null;
        }

        QDeal deal = QDeal.deal;
        return deal.store.storeId.in(storeIds);
    }

    /**
     * 작성자 필터 (특정 사용자가 작성한 딜)
     */
    public BooleanExpression createAuthorFilter(Long userId) {
        if (userId == null) {
            return null;
        }

        QDeal deal = QDeal.deal;
        return deal.user.userId.eq(userId);
    }

    /**
     * 좋아요 필터 (특정 사용자가 좋아요를 누른 딜)
     */
    public BooleanExpression createLikedByUserFilter(Long userId) {
        if (userId == null) {
            return null;
        }

        QDeal deal = QDeal.deal;
        QVote vote = QVote.vote;

        return JPAExpressions
                .selectFrom(vote)
                .where(vote.dealId.eq(deal)
                        .and(vote.userId.userId.eq(userId))
                        .and(vote.voteType.eq(VoteType.TRUE)))
                .exists();
    }

    /**
     * 댓글 필터 (특정 사용자가 댓글을 작성한 딜)
     */
    public BooleanExpression createCommentedByUserFilter(Long userId) {
        if (userId == null) {
            return null;
        }

        QDeal deal = QDeal.deal;
        QComment comment = QComment.comment;

        return JPAExpressions
                .selectFrom(comment)
                .where(comment.dealId.eq(deal)
                        .and(comment.userId.userId.eq(userId)))
                .exists();
    }

    /**
     * 시간 범위를 LocalDateTime으로 변환
     */
    private LocalDateTime resolveStartDate(TimeRangeType timeRangeType) {
        LocalDateTime now = LocalDateTime.now();

        return switch (timeRangeType) {
            case LAST3HOURS -> now.minusHours(3);
            case LAST6HOURS -> now.minusHours(6);
            case LAST12HOURS -> now.minusHours(12);
            case LAST24HOURS -> now.minusHours(24);
            case LAST3DAYS -> now.minusDays(3);
            case LAST7DAYS -> now.minusDays(7);
        };
    }

    /**
     * 여러 BooleanExpression을 AND로 결합
     */
    private BooleanExpression combineAnd(List<BooleanExpression> expressions) {
        BooleanExpression result = null;
        for (BooleanExpression expression : expressions) {
            if (expression != null) {
                result = result == null ? expression : result.and(expression);
            }
        }
        return result;
    }
}