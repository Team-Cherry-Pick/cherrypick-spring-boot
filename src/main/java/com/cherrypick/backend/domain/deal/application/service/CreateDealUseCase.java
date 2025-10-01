package com.cherrypick.backend.domain.deal.application.service;
import com.cherrypick.backend.domain.deal.application.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.domain.service.DealCreateService;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 딜 생성 유즈케이스입니다.
 *
 * 애플리케이션 레이어에서 딜 생성 요청을 처리하며,
 * 도메인 서비스와 인프라 서비스를 조합하여 전체 딜 생성 플로우를 관리합니다.
 *
 * @author gabury1
 * @since 1.0
 */
@Service @RequiredArgsConstructor
public class CreateDealUseCase {

    private final DealCreateService dealCreateService;
    private final ImageService imageService;

    /**
     * 새로운 딜을 생성합니다.
     *
     * 인증된 사용자 정보를 조회하고, 도메인 서비스를 통해 딜을 생성한 후,
     * 이미지 첨부 등의 부가 작업을 수행합니다.
     *
     * @param dto 딜 생성 요청 정보 (제목, 카테고리, URL, 가격, 할인 정보 등)
     * @return 생성된 딜의 ID와 성공 메시지를 포함한 응답 DTO
     * @throws BaseException 사용자 인증 실패, 딜 생성 실패, 이미지 첨부 실패 등의 경우
     */
    public DealResponseDTOs.Create createDeal(DealCreateRequestDTO dto) {

        // 인증된 유저인지 검사
        Long userId = AuthUtil.getUserDetail().userId();

        // 딜 생성 및 저장
        Long dealId = dealCreateService.createDeal(
                userId,
                dto.title(),
                dto.categoryId(),
                dto.originalUrl(),
                dto.storeId(),
                dto.storeName(),
                dto.price(),
                dto.shipping(),
                dto.content(),
                dto.discountIds(),
                dto.discountNames(),
                dto.discountDescription()
        );

        // 이미지 연결
        imageService.attachImage(dealId, dto.imageIds(), ImageType.DEAL);

        return new DealResponseDTOs.Create(dealId, "핫딜 게시글 생성 성공");
    }

}
