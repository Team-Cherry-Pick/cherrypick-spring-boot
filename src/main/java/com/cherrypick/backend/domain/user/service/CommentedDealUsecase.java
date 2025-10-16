package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.enums.SortType;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.service.DealSearchResponseFactory;
import com.cherrypick.backend.domain.deal.domain.service.search.DealFilterFactory;
import com.cherrypick.backend.domain.deal.domain.service.search.DealSortFactory;
import com.cherrypick.backend.global.util.AuthUtil;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service @RequiredArgsConstructor
public class CommentedDealUsecase
{
    private final DealRepository dealRepository;
    private final DealFilterFactory filterFactory;
    private final DealSortFactory sortFactory;
    private final DealSearchResponseFactory responseFactory;

    /**
     * 현재 인증된 사용자가 댓글을 작성한 딜을 페이징하여 조회합니다.
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>인증된 사용자 ID 추출</li>
     *   <li>해당 사용자가 댓글을 작성한 딜 목록 조회 (QueryDSL, 최신순)</li>
     *   <li>연관 데이터(카테고리, 스토어, 이미지, 통계) 포함한 응답 생성</li>
     * </ol>
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 아이템 수
     * @return 사용자가 댓글을 작성한 딜의 검색 응답 DTO (페이징 적용)
     * @throws com.cherrypick.backend.global.exception.BaseException 인증되지 않은 사용자인 경우
     */
    public DealSearchPageResponseDTO getCommentedDeals(int page, int size)
    {
        var userId = AuthUtil.getUserDetail().userId();

        // 필터 및 정렬 생성
        BooleanExpression filter = filterFactory.createCommentedByUserFilter(userId);
        var orders = sortFactory.createOrderSpecifiers(SortType.LATEST);
        Pageable pageable = PageRequest.of(0, 1000000);

        // 딜 조회
        List<Deal> deals = dealRepository.searchWithFilters(
                List.of(filter),
                orders,
                pageable
        ).getContent();

        return responseFactory.loadRelations(deals, false);
    }
}
