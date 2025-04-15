package com.cherrypick.backend.domain.deal.dto.request;

import com.cherrypick.backend.domain.deal.enums.SortType;
import com.cherrypick.backend.domain.deal.enums.TimeRangeType;
import com.cherrypick.backend.domain.deal.vo.Filter;
import com.cherrypick.backend.domain.deal.vo.PriceFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private SortType sortType;

    // 가격
    private PriceFilter priceFilter;

    // 다양한 가격 포함 여부
    private boolean variousPrice;

    // 할인 방식 (복수 선택)
    private List<Long> discountIds;

    // 스토어 (복수 선택)
    private List<Long> storeIds;
}
