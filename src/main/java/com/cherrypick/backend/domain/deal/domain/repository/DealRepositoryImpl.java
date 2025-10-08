package com.cherrypick.backend.domain.deal.domain.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.entity.QDeal;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

/**
 * DealRepositoryCustom 구현체
 *
 * <p>Spring Data JPA가 자동으로 감지 (네이밍 규칙: RepositoryImpl)</p>
 *
 * <p><b>주요 최적화 기법:</b></p>
 * <ul>
 *     <li>QueryDSL을 사용한 타입 안전 동적 쿼리</li>
 *     <li>fetchJoin을 통한 N+1 문제 해결</li>
 *     <li>Slice 패턴으로 COUNT 쿼리 없이 hasNext 판별</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class DealRepositoryImpl implements DealRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 동적 필터와 정렬을 적용한 딜 검색
     *
     * <p><b>작동 방식:</b></p>
     * <ol>
     *     <li>필터 리스트에 기본 조건(삭제되지 않은 딜) 추가</li>
     *     <li>size+1개 조회로 다음 페이지 존재 여부 판별</li>
     *     <li>fetchJoin으로 연관 엔티티 한 번에 로드 (N+1 방지)</li>
     *     <li>조회 결과가 size+1개면 hasNext=true, size개만 반환</li>
     * </ol>
     *
     * <p><b>성능 최적화:</b></p>
     * <ul>
     *     <li>COUNT 쿼리 불필요 (size+1 조회로 hasNext 판별)</li>
     *     <li>fetchJoin으로 1개 쿼리에 모든 연관 데이터 로드</li>
     *     <li>N+1 문제 완전 해결 (User, Category, Store, Discounts)</li>
     * </ul>
     *
     * @param filters QueryDSL BooleanExpression 필터 리스트 (동적 조건)
     * @param orders QueryDSL OrderSpecifier 정렬 리스트 (동적 정렬)
     * @param pageable 페이징 정보 (page, size)
     * @return Slice (content + hasNext, totalCount 없음)
     */
    @Override
    public Slice<Deal> searchWithFilters(
            List<BooleanExpression> filters,
            List<OrderSpecifier<?>> orders,
            Pageable pageable
    ) {
        QDeal deal = QDeal.deal;

        // ===== 쿼리 실행 =====
        // size+1 조회: hasNext 판별을 위해 요청한 size보다 1개 더 조회
        // 예: size=20 요청 → limit=21로 조회
        List<Deal> deals = queryFactory
                .selectFrom(deal)
                // fetchJoin: 연관 엔티티를 LEFT JOIN으로 한 번에 로드 (N+1 방지)
                // 일반 join은 Deal만 조회하지만, fetchJoin은 연관 엔티티도 함께 SELECT
                .leftJoin(deal.user).fetchJoin()          // User 즉시 로드
                .leftJoin(deal.category).fetchJoin()       // Category 즉시 로드
                .leftJoin(deal.discounts).fetchJoin()      // Discounts 즉시 로드 (컬렉션)
                .leftJoin(deal.store).fetchJoin()          // Store 즉시 로드
                .where(
                        // List<BooleanExpression>을 Predicate[] 배열로 변환
                        // QueryDSL의 where()는 가변인자(Predicate...)를 받음
                        // null인 조건은 자동으로 무시됨
                        Stream.concat(
                                filters.stream(),
                                Stream.of(deal.isDelete.eq(false))
                        ).toArray(Predicate[]::new)
                )
                .orderBy(
                        // List<OrderSpecifier<?>>를 배열로 변환
                        // 여러 정렬 조건 적용 가능 (예: heat DESC, createdAt DESC)
                        orders.toArray(new OrderSpecifier[0])
                )
                .offset(pageable.getOffset())              // 시작 위치 (page * size)
                .limit(pageable.getPageSize() + 1)         // size+1 조회 (hasNext 판별용)
                .fetch();

        // ===== hasNext 판별 =====
        // 조회된 결과가 요청한 size보다 크면 다음 페이지 존재
        // 예: size=20, 결과=21개 → hasNext=true
        //     size=20, 결과=15개 → hasNext=false
        boolean hasNext = deals.size() > pageable.getPageSize();

        // ===== 실제 반환 데이터 =====
        // hasNext=true면 마지막 1개 제외하고 size개만 반환
        // 예: 21개 조회 → 0~19번 인덱스(20개) 반환, 20번은 버림
        // hasNext=false면 조회된 전체 반환
        List<Deal> content = hasNext
                ? deals.subList(0, pageable.getPageSize())  // size개만 자르기
                : deals;                                     // 전체 반환

        // SliceImpl: Spring Data의 Slice 구현체
        // content: 실제 데이터, pageable: 페이징 정보, hasNext: 다음 페이지 존재 여부
        return new SliceImpl<>(content, pageable, hasNext);
    }
}
