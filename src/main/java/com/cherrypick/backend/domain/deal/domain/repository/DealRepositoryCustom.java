package com.cherrypick.backend.domain.deal.domain.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

/**
 * Deal 검색을 위한 커스텀 Repository 인터페이스
 */
public interface DealRepositoryCustom {

    /**
     * QueryDSL을 사용한 동적 검색 (Slice 반환)
     *
     * @param filters BooleanExpression 필터 리스트
     * @param orders OrderSpecifier 정렬 리스트
     * @param pageable 페이징 정보
     * @return Slice (hasNext 포함)
     */
    Slice<Deal> searchWithFilters(
            List<BooleanExpression> filters,
            List<OrderSpecifier<?>> orders,
            Pageable pageable
    );
}
