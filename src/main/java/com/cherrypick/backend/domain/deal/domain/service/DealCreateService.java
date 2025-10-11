package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.domain.deal.domain.entity.Category;
import com.cherrypick.backend.domain.deal.domain.repository.reference.CategoryRepository;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceVO;
import com.cherrypick.backend.domain.deal.domain.entity.vo.ShippingVO;
import com.cherrypick.backend.domain.deal.domain.port.DeepLinkConverter;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.util.ValidUrlUtil;
import com.cherrypick.backend.domain.deal.domain.entity.Discount;
import com.cherrypick.backend.domain.deal.domain.repository.reference.DiscountRepository;
import com.cherrypick.backend.domain.deal.domain.entity.Store;
import com.cherrypick.backend.domain.deal.domain.repository.reference.StoreRepository;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.LinkPriceErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 딜 생성 도메인 서비스입니다.
 *
 * 딜 생성에 필요한 비즈니스 로직을 캡슐화하며,
 * 사용자 검증, 엔티티 조회, 할인 처리, 딥링크 생성 등의 책임을 가집니다.
 *
 * @author gabury1
 * @since 1.0
 */
@Service @RequiredArgsConstructor
public class DealCreateService
{
    private final DealRepository dealRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final DiscountRepository discountRepository;
    private final DeepLinkConverter deepLinkConverter;


    /**
     * 새로운 딜을 생성합니다.
     *
     * 사용자 인증, 카테고리 및 스토어 검증을 수행하고,
     * 할인 정보를 처리한 후 딥링크를 생성하여 딜을 저장합니다.
     *
     * @param userId 딜을 작성하는 사용자의 ID
     * @param title 딜 제목
     * @param categoryId 딜이 속할 카테고리 ID
     * @param originalUrl 딜의 원본 상품 URL (단축 URL 차단)
     * @param storeId 딜의 스토어 ID (null 가능)
     * @param storeName 딜의 스토어명 (storeId가 null일 때 사용)
     * @param price 가격 정보 (정가, 할인가, 통화 등)
     * @param shipping 배송 정보 (배송비, 배송 타입 등)
     * @param content 딜 상세 내용
     * @param discountIds 적용할 할인 ID 목록 (null 가능)
     * @param discountNames 커스텀 할인명 목록 (null 가능)
     * @param discountDescription 할인 상세 설명 (null 가능)
     * @return 생성된 딜의 ID
     * @throws BaseException 사용자 인증 실패, 카테고리/스토어/할인 미존재, 잘못된 URL 등의 경우
     */
    public Long createDeal(Long userId,
                           String title,
                           Long categoryId,
                           String originalUrl,
                           Long storeId,
                           String storeName,
                           PriceVO price,
                           ShippingVO shipping,
                           String content,
                           List<Long> discountIds,
                           List<String> discountNames,
                           String discountDescription
                           ) throws BaseException
    {
        // 1. 엔티티 검증 및 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));

        Store store = null;
        if (storeId != null) {
            store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new BaseException(DealErrorCode.STORE_NOT_FOUND));
        }

        // 2. 할인 정보 처리
        List<Discount> discounts = processDiscounts(discountIds);
        String discountName = Deal.createDiscountName(discountNames);

        // 3. URL 검증 및 딥링크 생성
        String deepLink = null;
        if (ValidUrlUtil.isValidUrl(originalUrl)) {
            try{
                deepLink = deepLinkConverter.createDeeplink(originalUrl);
            } catch (BaseException e) {
                System.out.println(e.getMessage());
            }

        }
        else {
            throw new BaseException(LinkPriceErrorCode.INVALID_ORIGINAL_URL);
        }

        // 4. Deal 엔티티 생성 및 저장
        Deal deal = Deal.builder()
                .user(user)
                .title(title)
                .category(category)
                .originalUrl(originalUrl)
                .deepLink(deepLink)
                .store(store)
                .storeName(store == null ? storeName : null)
                .price(price)
                .shipping(shipping)
                .content(content)
                .discounts(discounts)
                .discountName(discountName)
                .discountDescription(discountDescription)
                .isSoldOut(false)
                .heat(0.0)
                .totalViews(0L)
                .isDelete(false)
                .build();

        Deal saved = dealRepository.save(deal);

        return saved.getDealId();

    }

    /**
     * 할인 ID 목록을 검증하고 할인 엔티티 목록으로 변환합니다.
     *
     * @param discountIds 할인 ID 목록 (null 또는 빈 리스트 가능)
     * @return 검증된 할인 엔티티 목록
     * @throws BaseException 할인 ID가 존재하지 않는 경우
     */
    private List<Discount> processDiscounts(List<Long> discountIds) {
        List<Discount> discounts = new ArrayList<>();

        if (discountIds != null && !discountIds.isEmpty()) {
            List<Discount> foundDiscounts = discountRepository.findAllById(discountIds);
            if (foundDiscounts.size() != discountIds.size()) {
                throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
            }
            discounts.addAll(foundDiscounts);
        }

        return discounts;
    }
}
