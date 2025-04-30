package com.cherrypick.backend.domain.deal.dto.response;

import java.util.List;

public record DealSearchPageResponseDTO(
        List<DealSearchResponseDTO> deals,
        boolean hasNext // 다음 페이지 여부
) {}

