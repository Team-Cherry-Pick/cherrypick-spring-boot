package com.cherrypick.backend.domain.deal.application.service;

import com.cherrypick.backend.domain.deal.application.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.service.DealEnrichmentService;
import com.cherrypick.backend.domain.deal.domain.service.DealValidationService;
import com.cherrypick.backend.domain.deal.domain.service.search.DealFilterService;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SearchDealUsecase {

    private final DealValidationService validationService;
    private final DealFilterService filterService;
    private final DealEnrichmentService enrichmentService;
    private final DealRepository dealRepository;

    public DealSearchPageResponseDTO searchDeals(DealSearchRequestDTO dto, int page, int size) {

        // 1단계: 유효성 검증
        validateSearchRequest(dto);

        // 2단계: 필터 조립
        List<BooleanExpression> filters = buildFilters(dto);

        // 3단계: 검색 실행
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<Deal> deals = dealRepository.searchWithFilters(filters, pageable);

        // TODO: 4단계 - 정렬 (인메모리 정렬이 필요한 경우)

        // TODO: 5단계 - DTO 변환
        var response = enrichmentService.loadRelations(deals, 0, 20);


        return response;
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

    /**
     * QueryDSL 필터 조립
     */
    private List<BooleanExpression> buildFilters(DealSearchRequestDTO dto) {
        List<BooleanExpression> filters = new ArrayList<>();

        // 기본 필터
        filters.add(filterService.createDeleteFilter());

        // 검색 조건 필터
        filters.add(filterService.createCategoryFilter(dto.getCategoryId()));
        filters.add(filterService.createKeywordFilter(dto.getKeyword()));

        // 일반 필터 (품절, 배송)
        filters.add(filterService.createSoldOutFilter(dto.getFilters()));
        filters.add(filterService.createFreeShippingFilter(dto.getFilters()));
        filters.add(filterService.createGlobalShippingFilter(dto.getFilters()));

        // 날짜/가격 필터
        filters.add(filterService.createDateRangeFilter(dto.getTimeRange()));
        filters.add(filterService.createPriceFilter(dto.getPriceFilter(), dto.getVariousPrice()));

        // 할인/스토어 필터
        filters.add(filterService.createDiscountFilter(dto.getDiscountIds()));
        filters.add(filterService.createStoreFilter(dto.getStoreIds()));

        // null 제거 후 반환
        return filters.stream()
                .filter(Objects::nonNull)
                .toList();
    }

}
