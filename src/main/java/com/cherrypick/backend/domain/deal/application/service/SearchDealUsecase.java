package com.cherrypick.backend.domain.deal.application.service;

import com.cherrypick.backend.domain.deal.application.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.domain.service.DealValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchDealUsecase {

    private final DealValidationService validationService;

    public DealSearchPageResponseDTO searchDeals(DealSearchRequestDTO dto, int page, int size) {

        // 1단계: 유효성 검증
        validateSearchRequest(dto);

        // TODO: 2단계 - 필터 파라미터 변환
        // TODO: 3단계 - 검색 실행
        // TODO: 4단계 - 정렬
        // TODO: 5단계 - DTO 변환

        return null;
    }

    /**
     * 검색 요청 유효성 검증
     */
    private void validateSearchRequest(DealSearchRequestDTO dto) {
        // 카테고리 존재 확인
        validationService.validateCategoryExists(dto.getCategoryId());

        // 할인 ID 존재 확인
        validationService.validateDiscountIdsExist(dto.getDiscountIds());

        // 스토어 ID 존재 확인
        validationService.validateStoreIdsExist(dto.getStoreIds());

        // 가격 필터 논리 검증
        validationService.validatePriceFilter(dto.getPriceFilter());
    }



}
