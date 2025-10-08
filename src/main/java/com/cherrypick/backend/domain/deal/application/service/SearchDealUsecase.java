package com.cherrypick.backend.domain.deal.application.service;

import com.cherrypick.backend.domain.deal.application.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.service.DealSearchResponseFactory;
import com.cherrypick.backend.domain.deal.domain.service.DealValidationService;
import com.cherrypick.backend.domain.deal.domain.service.search.DealFilterFactory;
import com.cherrypick.backend.domain.deal.domain.service.search.DealSortFactory;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SearchDealUsecase {

    private final DealValidationService validationService;
    private final DealFilterFactory filterFactory;
    private final DealSortFactory sortFactory;
    private final DealSearchResponseFactory responseFactory;
    private final DealRepository dealRepository;

    public DealSearchPageResponseDTO searchDeals(DealSearchRequestDTO dto, int page, int size) {

        // 1단계: 유효성 검증
        validateSearchRequest(dto);

        // 2단계: 필터 조립
        List<BooleanExpression> filters = buildFilters(dto);

        // 3단계: 정렬 조립
        List<OrderSpecifier<?>> orders = sortFactory.createOrderSpecifiers(dto.getSortType());

        // 4단계: 검색 실행 (DB 필터링 + 정렬 + 페이징)
        Pageable pageable = PageRequest.of(page, size);
        Slice<Deal> dealSlice = dealRepository.searchWithFilters(filters, orders, pageable);

        // 5단계: DTO 변환
        var response = responseFactory.loadRelations(dealSlice.getContent(), dealSlice.hasNext());

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

        // 검색 조건 필터
        filters.add(filterFactory.createCategoryFilter(dto.getCategoryId()));
        filters.add(filterFactory.createKeywordFilter(dto.getKeyword()));

        // 일반 필터 (품절, 배송)
        filters.add(filterFactory.createSoldOutFilter(dto.getFilters()));
        filters.add(filterFactory.createFreeShippingFilter(dto.getFilters()));
        filters.add(filterFactory.createGlobalShippingFilter(dto.getFilters()));

        // 날짜/가격 필터
        filters.add(filterFactory.createDateRangeFilter(dto.getTimeRange()));
        filters.add(filterFactory.createPriceFilter(dto.getPriceFilter(), dto.getVariousPrice()));

        // 할인/스토어 필터
        filters.add(filterFactory.createDiscountFilter(dto.getDiscountIds()));
        filters.add(filterFactory.createStoreFilter(dto.getStoreIds()));

        // null 제거 후 반환
        return filters.stream()
                .filter(Objects::nonNull)
                .toList();
    }

}
