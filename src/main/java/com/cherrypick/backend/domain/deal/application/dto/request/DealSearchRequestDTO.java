package com.cherrypick.backend.domain.deal.application.dto.request;

import com.cherrypick.backend.domain.deal.domain.enums.SortType;
import com.cherrypick.backend.domain.deal.domain.enums.TimeRangeType;
import com.cherrypick.backend.domain.deal.domain.entity.vo.Filter;
import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceFilter;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealSearchRequestDTO {

    // 카테고리 ID
    private Long categoryId;

    // 검색어 (제목, 본문)
    private String keyword;

    // 기본 필터
    private Filter filters;

    // 시간 기준
    private TimeRangeType timeRange;

    // 정렬 기준
    @Builder.Default
    private SortType sortType = SortType.LATEST;

    // 가격
    private PriceFilter priceFilter;

    // 다양한 가격 포함 여부
    private Boolean variousPrice;

    // 할인 방식 (복수 선택)
    private List<Long> discountIds;

    // 스토어 (복수 선택)
    private List<Long> storeIds;
}
