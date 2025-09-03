package com.cherrypick.backend.domain.linkprice.dto.request;

import java.util.Optional;

public record LinkPriceRequest(
        String yyyymmdd,          // 조회일자 (필수, YYYYMMDD 또는 YYYYMM)
        String cancelFlag,        // 취소여부 (옵션: Y/N)
        String currency,          // 통화 코드 (옵션: KRW, USD 등)
        String merchantId,        // 머천트 ID (옵션)
        Integer page,             // 페이지 번호 (옵션)
        Integer perPage,          // 페이지 당 데이터 수 (옵션, 기본 1000)
        String test               // 테스트 여부 (옵션: Y)
) {
    // Optional로 변환해서 null 체크 용이하게 함
    public Optional<String> cancelFlagOpt() { return Optional.ofNullable(cancelFlag); }
    public Optional<String> currencyOpt() { return Optional.ofNullable(currency); }
    public Optional<String> merchantIdOpt() { return Optional.ofNullable(merchantId); }
    public Optional<Integer> pageOpt() { return Optional.ofNullable(page); }
    public Optional<Integer> perPageOpt() { return Optional.ofNullable(perPage); }
    public Optional<String> testOpt() { return Optional.ofNullable(test); }
}

