package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.domain.deal.domain.entity.Category;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.repository.reference.CategoryRepository;
import com.cherrypick.backend.domain.deal.domain.entity.vo.PriceFilter;
import com.cherrypick.backend.domain.deal.domain.entity.Discount;
import com.cherrypick.backend.domain.deal.domain.repository.reference.DiscountRepository;
import com.cherrypick.backend.domain.deal.domain.entity.Store;
import com.cherrypick.backend.domain.deal.domain.repository.reference.StoreRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Deal 도메인의 유효성 검증을 담당하는 서비스
 * - 엔티티 존재 여부 검증
 * - 비즈니스 규칙 검증
 * - 엔티티 조회 및 검증
 */
@Service
@RequiredArgsConstructor
public class DealValidationService {

    private final CategoryRepository categoryRepository;
    private final DiscountRepository discountRepository;
    private final StoreRepository storeRepository;
    private final DealRepository dealRepository;

    /**
     * 딜 존재 여부 검증 및 조회
     *
     * @param dealId 딜 ID
     * @return 조회된 딜 엔티티
     * @throws BaseException 딜이 존재하지 않는 경우 (DEAL_NOT_FOUND)
     */
    public Deal getValidatedDeal(Long dealId){

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        return deal;
    }

    /**
     * 카테고리 존재 여부 검증
     *
     * @param categoryId 카테고리 ID (null 가능)
     * @throws BaseException 카테고리가 존재하지 않는 경우 (CATEGORY_NOT_FOUND)
     */
    public void validateCategoryExists(Long categoryId) {
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new BaseException(DealErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    /**
     * 할인 ID 목록의 존재 여부 검증
     * - 먼저 ID 형식을 검증한 후 DB 존재 여부를 확인
     *
     * @param discountIds 할인 ID 목록 (null 또는 empty 가능)
     * @throws BaseException 할인 ID 형식이 잘못되었거나 존재하지 않는 경우 (INVALID_DISCOUNT_INFORMATION, DISCOUNT_NOT_FOUND)
     */
    public void validateDiscountIdsExist(List<Long> discountIds) {
        if (discountIds == null || discountIds.isEmpty()) {
            return;
        }

        // ID 형식 검증
        validateDiscountIds(discountIds);

        // 존재 여부 검증
        List<Long> foundDiscountIds = discountRepository.findAllById(discountIds)
                .stream()
                .map(Discount::getDiscountId)
                .toList();

        if (foundDiscountIds.size() != discountIds.size()) {
            throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
        }
    }

    /**
     * 스토어 ID 목록의 존재 여부 검증
     *
     * @param storeIds 스토어 ID 목록 (null 또는 empty 가능)
     * @throws BaseException 스토어가 존재하지 않는 경우 (STORE_NOT_FOUND)
     */
    public void validateStoreIdsExist(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) {
            return;
        }

        List<Long> foundStoreIds = storeRepository.findAllById(storeIds)
                .stream()
                .map(Store::getStoreId)
                .toList();

        if (foundStoreIds.size() != storeIds.size()) {
            throw new BaseException(DealErrorCode.STORE_NOT_FOUND);
        }
    }

    /**
     * 카테고리 조회 및 검증
     *
     * @param categoryId 카테고리 ID
     * @return 조회된 카테고리 엔티티
     * @throws BaseException 카테고리가 존재하지 않는 경우 (CATEGORY_NOT_FOUND)
     */
    public Category getValidatedCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * 할인 목록 조회 및 검증
     *
     * @param discountIds 할인 ID 목록 (null 또는 empty 가능)
     * @return 조회된 할인 엔티티 목록 (입력이 null/empty인 경우 빈 리스트 반환)
     * @throws BaseException 할인이 존재하지 않는 경우 (DISCOUNT_NOT_FOUND)
     */
    public List<Discount> getValidatedDiscounts(List<Long> discountIds) {
        if (discountIds == null || discountIds.isEmpty()) {
            return List.of();
        }

        List<Discount> foundDiscounts = discountRepository.findAllById(discountIds);

        if (foundDiscounts.size() != discountIds.size()) {
            throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
        }

        return foundDiscounts;
    }

    /**
     * 스토어 조회 및 검증
     *
     * @param storeId 스토어 ID (null 가능)
     * @return 조회된 스토어 엔티티 (storeId가 null인 경우 null 반환)
     * @throws BaseException 스토어가 존재하지 않는 경우 (STORE_NOT_FOUND)
     */
    public Store getValidatedStore(Long storeId) {
        if (storeId == null) {
            return null;
        }

        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BaseException(DealErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 가격 필터 유효성 검증
     * - minPrice 또는 maxPrice가 있으면 priceType이 필수
     *
     * @param priceFilter 가격 필터 (null 가능)
     * @throws BaseException 가격 범위는 있으나 가격 타입이 없는 경우 (INVALID_PRICE_TYPE)
     */
    public void validatePriceFilter(PriceFilter priceFilter) {
        if (priceFilter == null) {
            return;
        }

        if ((priceFilter.minPrice() != null || priceFilter.maxPrice() != null)
                && priceFilter.priceType() == null) {
            throw new BaseException(DealErrorCode.INVALID_PRICE_TYPE);
        }
    }

    /**
     * 할인 ID 형식 검증 (null 또는 양수가 아닌 값 체크)
     *
     * @param discountIds 할인 ID 목록 (null 또는 empty 가능)
     * @throws BaseException 할인 ID가 null이거나 0 이하인 경우 (INVALID_DISCOUNT_INFORMATION)
     */
    public void validateDiscountIds(List<Long> discountIds) {
        if (discountIds == null || discountIds.isEmpty()) {
            return;
        }

        for (Long discountId : discountIds) {
            if (discountId == null || discountId <= 0) {
                throw new BaseException(DealErrorCode.INVALID_DISCOUNT_INFORMATION);
            }
        }
    }
}
