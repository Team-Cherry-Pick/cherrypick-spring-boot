package com.cherrypick.backend.domain.deal.domain.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.entity.QDeal;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DealRepositoryCustom 구현체
 * Spring Data JPA가 자동으로 감지 (네이밍 규칙: RepositoryImpl)
 */
@Repository
@RequiredArgsConstructor
public class DealRepositoryImpl implements DealRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Deal> searchWithFilters(List<BooleanExpression> filters, Pageable pageable) {
        QDeal deal = QDeal.deal;

        return queryFactory
                .selectFrom(deal)
                .leftJoin(deal.user).fetchJoin()
                .leftJoin(deal.category).fetchJoin()
                .leftJoin(deal.discounts).fetchJoin()
                .leftJoin(deal.store).fetchJoin()
                .where(filters.toArray(new Predicate[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
