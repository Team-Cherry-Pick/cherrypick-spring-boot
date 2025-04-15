package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.enums.ShippingType;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.discount.entity.Discount;
import com.cherrypick.backend.domain.discount.repository.DiscountRepository;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.config.oauth.UserDetailDTO;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;
    private final DiscountRepository discountRepository;
    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 게시글 생성
    @Transactional
    public DealResponseDTOs.Create createDeal(DealCreateRequestDTO dto) {

        // TODO: 이미지 처리, 딥링크 변환

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));

        Store store = null;
        if (dto.storeId() != null) {
            store = storeRepository.findById(dto.storeId())
                    .orElseThrow(() -> new BaseException(DealErrorCode.STORE_NOT_FOUND));
        }

        List<Discount> discounts = new ArrayList<>();

        String discountName = null;

        if (dto.discountIds() != null && !dto.discountIds().isEmpty()) {
            List<Discount> foundDiscounts = discountRepository.findAllById(dto.discountIds());
            if (foundDiscounts.size() != dto.discountIds().size()) {
                throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
            }
            discounts.addAll(foundDiscounts);
        }

        if (dto.discountNames() != null && !dto.discountNames().isEmpty()) {
            // discountNames "카드, 쿠폰" 형식으로 한 컬럼에 저장
            discountName = String.join(", ", dto.discountNames());
        }

        Deal deal = Deal.builder()
                .userId(user)
                .title(dto.title())
                .categoryId(category)
                .originalUrl(dto.originalUrl())
                .storeId(store)
                .storeName(store == null ? dto.storeName() : null)
                .price(dto.price())
                .shipping(dto.shipping())
                .content(dto.content())
                .discounts(discounts)
                .discountName(discountName)
                .isSoldOut(false)
                .build();

        Deal saved = dealRepository.save(deal);

        return new DealResponseDTOs.Create(saved.getDealId(), "핫딜 게시글 생성 성공");
    }

    // 게시글 상세조회
    @Transactional
    public DealDetailResponseDTO getDealDetail(Long dealId) {

        // Deal이 존재하는지 확인
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 카테고리 정보
        List<String> categorys = List.of(deal.getCategoryId().getName());

        // User 엔티티 정보
        com.cherrypick.backend.domain.user.vo.User userVo = new com.cherrypick.backend.domain.user.vo.User(
                deal.getUserId().getUserId(),
                deal.getUserId().getNickname(),
                null // TODO: 이미지 URL
        );

        // Store 정보
        com.cherrypick.backend.domain.store.vo.Store storeVo = null;
        if (deal.getStoreId() != null) {
            // Store ID가 있다면 해당 스토어 정보 가져오기
            storeVo = new com.cherrypick.backend.domain.store.vo.Store(
                    deal.getStoreId().getName(),
                    deal.getStoreId().getTextColor(),
                    deal.getStoreId().getBackgroundColor()
            );
        } else if (deal.getStoreName() != null) {
            // Store Name만 있으면 컬러 정보는 null로 처리
            storeVo = new com.cherrypick.backend.domain.store.vo.Store(
                    deal.getStoreName(),
                    null,
                    null
            );
        }

        List<String> infoTags = getInfoTags(deal);

        // 좋아요/싫어요 수 조회
        long likeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.TRUE);
        long dislikeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.FALSE);

        // 댓글 수 조회
        long commentCount = commentRepository.countByDealId_DealIdAndIsDeleteFalse(dealId);

        // Redis 조회수 로직
        String dealViewKey = "deal:view:" + dealId;
        redisTemplate.opsForValue().increment(dealViewKey, 1);

        Object redisViewValue = redisTemplate.opsForValue().get(dealViewKey);
        long totalViews = redisViewValue == null ? 0L : ((Number) redisViewValue).longValue();

        return new DealDetailResponseDTO(
                deal.getDealId(),
                List.of(), // TODO: 이미지 URL LIST
                userVo,
                storeVo,
                categorys,
                deal.getTitle(),
                infoTags,
                deal.getShipping(), // TODO: 한화 int 처리
                deal.getPrice(), // TODO: 한화 int 처리
                deal.getContent(),
                (int) totalViews,
                (int) likeCount,
                (int) dislikeCount,
                (int) commentCount,
                deal.getDeepLink(),
                deal.getOriginalUrl(),
                deal.isSoldOut()
        );
    }

    // 게시글 수정
    @Transactional
    public DealResponseDTOs.Update updateDeal(DealUpdateRequestDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        Deal deal = dealRepository.findById(dto.dealId())
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 작성자 검증
        if (!deal.getUserId().getUserId().equals(userDetails.userId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        if (dto.title() != null) {
            deal.setTitle(dto.title());
        }

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));
            deal.setCategoryId(category);
        }

        if (dto.originalUrl() != null) {
            deal.setOriginalUrl(dto.originalUrl());
        }

        Store store = null;
        if (dto.storeId() != null) {
            store = storeRepository.findById(dto.storeId())
                    .orElseThrow(() -> new BaseException(DealErrorCode.STORE_NOT_FOUND));
            deal.setStoreId(store);
            deal.setStoreName(null); // storeId 있으면 storeName은 무시
        } else if (dto.storeName() != null) {
            deal.setStoreId(null);
            deal.setStoreName(dto.storeName());
        }

        if (dto.price() != null) {
            deal.setPrice(dto.price());
        }

        if (dto.shipping() != null) {
            deal.setShipping(dto.shipping());
        }

        if (dto.content() != null) {
            deal.setContent(dto.content());
        }

        if (dto.discountIds() != null) {
            List<Discount> foundDiscounts = discountRepository.findAllById(dto.discountIds());
            if (foundDiscounts.size() != dto.discountIds().size()) {
                throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
            }
            deal.setDiscounts(foundDiscounts);
        }

        if (dto.discountNames() != null) {
            String discountName = String.join(", ", dto.discountNames());
            deal.setDiscountName(discountName);
        }

        if (dto.isSoldOut()) {
            deal.setSoldOut(true); // dto.isSoldOut()이 true일 경우
        } else {
            deal.setSoldOut(false); // dto.isSoldOut()이 false일 경우
        }

        return new DealResponseDTOs.Update(deal.getDealId(), "핫딜 게시글 수정 성공");
    }

    // 게시글 삭제 (Soft Delete)
    @Transactional
    public DealResponseDTOs.Delete deleteDeal(Long dealId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 작성자 검증
        if (!deal.getUserId().getUserId().equals(userDetails.userId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        deal.setIsDelete(true);

        return new DealResponseDTOs.Delete("핫딜 게시글 삭제 성공");
    }

    // 인포 태그 생성 메소드
    @NotNull
    private static List<String> getInfoTags(Deal deal) {
        List<String> infoTags = new ArrayList<>();

        // 배송 타입이 FREE이면 #무료배송 추가
        if (deal.getShipping() != null && deal.getShipping().shippingType() == ShippingType.FREE) {
            infoTags.add("#무료배송");
        }

        // 할인 ID가 있다면 해당 할인 ID의 이름에 해시태그 추가
        if (deal.getDiscounts() != null && !deal.getDiscounts().isEmpty()) {
            for (Discount discount : deal.getDiscounts()) {
                infoTags.add("#" + discount.getName());
            }
        }

        // 할인 이름이 있다면, 카드나 쿠폰 이름을 해시태그로 추가
        if (deal.getDiscountName() != null && !deal.getDiscountName().isEmpty()) {
            String[] discountNames = deal.getDiscountName().split(", ");
            for (String discountName : discountNames) {
                infoTags.add("#" + discountName);
            }
        }
        return infoTags;
    }

}
