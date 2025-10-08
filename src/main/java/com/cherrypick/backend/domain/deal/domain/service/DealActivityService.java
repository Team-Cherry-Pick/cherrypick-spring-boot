package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.enums.SortType;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.service.search.DealFilterFactory;
import com.cherrypick.backend.domain.deal.domain.service.search.DealSortFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class DealActivityService
{
    private final DealRepository dealRepository;
    private final DealFilterFactory filterFactory;
    private final DealSortFactory sortFactory;

    /**
     * 특정 사용자가 작성한 모든 딜을 조회합니다. (QueryDSL, 최신순)
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 딜 목록 (빈 리스트 가능)
     */
    public List<Deal> getDealsByAuthor(Long userId)
    {
        BooleanExpression filter = filterFactory.createAuthorFilter(userId);
        var orders = sortFactory.createOrderSpecifiers(SortType.LATEST);
        Pageable pageable = PageRequest.of(0, 1000000);

        return dealRepository.searchWithFilters(
                List.of(filter),
                orders,
                pageable
        ).getContent();
    }

    /**
     * 특정 사용자가 좋아요(추천)를 누른 모든 딜을 조회합니다. (QueryDSL, 최신순)
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 좋아요를 누른 딜 목록 (빈 리스트 가능)
     */
    public List<Deal> getLikedDealsByUserId(Long userId)
    {
        BooleanExpression filter = filterFactory.createLikedByUserFilter(userId);
        var orders = sortFactory.createOrderSpecifiers(SortType.LATEST);
        Pageable pageable = PageRequest.of(0, 1000000);

        return dealRepository.searchWithFilters(
                List.of(filter),
                orders,
                pageable
        ).getContent();
    }

    /**
     * 특정 사용자가 댓글을 작성한 모든 딜을 조회합니다. (QueryDSL, 최신순)
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 댓글을 작성한 딜 목록 (중복 제거, 빈 리스트 가능)
     */
    public List<Deal> getCommentedDealsByUserId(Long userId)
    {
        BooleanExpression filter = filterFactory.createCommentedByUserFilter(userId);
        var orders = sortFactory.createOrderSpecifiers(SortType.LATEST);
        Pageable pageable = PageRequest.of(0, 1000000);

        return dealRepository.searchWithFilters(
                List.of(filter),
                orders,
                pageable
        ).getContent();
    }


}
