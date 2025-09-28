package com.cherrypick.backend.domain.deal.domain.port;

import com.cherrypick.backend.global.exception.BaseException;

/**
 * 딥링크 변환 포트입니다.
 *
 * 외부 API를 통해 원본 상품 URL을 제휴사 딥링크로 변환하는 기능을 정의합니다.
 * 이 인터페이스는 도메인 레이어에서 정의되고 어댑터 레이어에서 구현됩니다.
 *
 * @author gabury1
 * @since 1.0
 */
public interface DeepLinkConverter {

    /**
     * 원본 상품 URL을 제휴사 딥링크로 변환합니다.
     *
     * 외부 API(LinkPrice 등)를 호출하여 제휴사 링크를 생성하고,
     * 수수료 추적이 가능한 딥링크 URL을 반환합니다.
     *
     * @param originalUrl 변환할 원본 상품 URL (유효한 URL이어야 함)
     * @return 변환된 딥링크 URL
     * @throws BaseException 외부 API 호출 실패, 지원하지 않는 쇼핑몰, 네트워크 오류 등의 경우
     */
    String createDeeplink(String originalUrl) throws BaseException;
}
