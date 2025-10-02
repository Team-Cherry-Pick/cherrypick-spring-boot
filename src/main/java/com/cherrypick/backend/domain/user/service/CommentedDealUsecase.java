package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.domain.service.DealActivityService;
import com.cherrypick.backend.domain.deal.domain.service.DealEnrichmentService;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CommentedDealUsecase
{
    private final DealActivityService dealActivityService;
    private final DealEnrichmentService dealEnrichmentService;

    /**
     * 현재 인증된 사용자가 댓글을 작성한 모든 딜을 조회합니다.
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>인증된 사용자 ID 추출</li>
     *   <li>해당 사용자가 댓글을 작성한 딜 목록 조회</li>
     *   <li>연관 데이터(카테고리, 스토어, 이미지, 통계) 포함한 응답 생성</li>
     * </ol>
     *
     * @return 사용자가 댓글을 작성한 모든 딜의 검색 응답 DTO (페이징 없이 전체 반환)
     * @throws com.cherrypick.backend.global.exception.BaseException 인증되지 않은 사용자인 경우
     */
    public DealSearchPageResponseDTO getCommentedDeals()
    {
        var userId = AuthUtil.getUserDetail().userId();
        var deals = dealActivityService.getCommentedDealsByUserId(userId);

        return dealEnrichmentService.loadRelations(deals, 0, deals.size());
    }
}
