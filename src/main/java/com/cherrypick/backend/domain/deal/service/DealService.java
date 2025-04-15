package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.enums.PriceType;
import com.cherrypick.backend.domain.deal.enums.ShippingType;
import com.cherrypick.backend.domain.deal.enums.SortType;
import com.cherrypick.backend.domain.deal.enums.TimeRangeType;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    // 게시글 전체조회 (검색)
    @Transactional
    public List<DealSearchResponseDTO> searchDeals(DealSearchRequestDTO request) {
        // 시간 범위 필터
        LocalDateTime startDate = resolveStartDate(request.getTimeRange());
        LocalDateTime endDate = LocalDateTime.now();

        // 가격 필터
        Double minPrice = request.getPriceFilter() != null ? request.getPriceFilter().minPrice() : null;
        Double maxPrice = request.getPriceFilter() != null ? request.getPriceFilter().maxPrice() : null;
        PriceType priceType = request.getPriceFilter() != null
                ? request.getPriceFilter().priceType()
                : null;

        // 기본 필터
        boolean viewSoldOut = request.getFilters() != null && request.getFilters().viewSoldOut();
        boolean freeShipping = request.getFilters() != null && request.getFilters().freeShipping();
        boolean variousPrice = request.isVariousPrice();

        // 할인, 스토어
        List<Long> discountIds = (request.getDiscountIds() == null || request.getDiscountIds().isEmpty())
                ? null : request.getDiscountIds();
        List<Long> storeIds = (request.getStoreIds() == null || request.getStoreIds().isEmpty())
                ? null : request.getStoreIds();

        // 가격 정렬 여부 판단
        boolean sortPriceHigh = request.getSortType() == SortType.PRICE_HIGH;
        boolean sortPriceLow = request.getSortType() == SortType.PRICE_LOW;

        // DB 조회 + 가격 정렬 (쿼리에서 처리)
        List<Deal> deals = dealRepository.searchDeals(
                request.getCategoryId(),
                request.getKeyword(),
                viewSoldOut,
                freeShipping,
                startDate,
                endDate,
                minPrice,
                maxPrice,
                priceType,
                variousPrice,
                discountIds,
                storeIds,
                sortPriceHigh,
                sortPriceLow
        );

        // 저가순, 고가순을 제외한 정렬
        if (!sortPriceHigh && !sortPriceLow) {
            deals = sortDeals(deals, request.getSortType());
        }

        // 응답 매핑
        return deals.stream().map(deal -> {
            // 조회수는 증가 없이 조회만
            String key = "deal:view:" + deal.getDealId();
            Object redisVal = redisTemplate.opsForValue().get(key);
            long viewCount = redisVal == null ? 0L : ((Number) redisVal).longValue();

            long likeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.TRUE);
            long commentCount = commentRepository.countByDealId_DealIdAndIsDeleteFalse(deal.getDealId());

            return new DealSearchResponseDTO(
                    deal.getDealId(),
                    null, // TODO: 이미지 처리
                    deal.getTitle(),
                    deal.getStoreId() != null ? deal.getStoreId().getName() : deal.getStoreName(),
                    getInfoTags(deal),
                    deal.getPrice(),
                    deal.getCreatedAt().toString(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();
    }

    // 게시글 상세조회
    @Transactional
    public DealDetailResponseDTO getDealDetail(Long dealId) {

        // Deal이 존재하는지 확인
        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 카테고리 정보
        List<String> categoryNames = new ArrayList<>();
        Long categoryId = deal.getCategoryId().getCategoryId();

        // 카테고리 이름을 부모에서 자식 순으로 조회
        while (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));

            categoryNames.add(0, category.getName());  // 부모부터 자식 순으로 저장하기 위해 앞에 추가
            categoryId = category.getParentId();  // 부모 카테고리로 이동
        }

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

        // 인포 태그 생성
        List<String> infoTags = getInfoTags(deal);

        // 조회수 증가
        String viewKey = "deal:view:" + deal.getDealId();
        redisTemplate.opsForValue().increment(viewKey, 1);
        Object redisVal = redisTemplate.opsForValue().get(viewKey);
        long totalViews = redisVal == null ? 0L : ((Number) redisVal).longValue();

        // 매트릭스 조회 (조회수, 좋아요 수, 싫어요 수, 댓글 수)
        long[] metrics = getDealMetrics(deal);

        return new DealDetailResponseDTO(
                deal.getDealId(),
                List.of(), // TODO: 이미지 URL LIST
                userVo,
                storeVo,
                categoryNames,
                deal.getTitle(),
                infoTags,
                deal.getShipping(), // TODO: 한화 int 처리
                deal.getPrice(), // TODO: 한화 int 처리
                deal.getContent(),
                (int) totalViews,
                (int) metrics[1], // likeCount
                (int) metrics[2], // dislikeCount
                (int) metrics[3], // commentCount
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

    // 핫딜 투표, 댓글수 조회
    private long[] getDealMetrics(Deal deal) {
        long likeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.TRUE);
        long dislikeCount = voteRepository.countByDealIdAndVoteType(deal, VoteType.FALSE);
        long commentCount = commentRepository.countByDealId_DealIdAndIsDeleteFalse(deal.getDealId());

        return new long[]{likeCount, dislikeCount, commentCount};
    }

    // 인포 태그 생성 메소드
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

    // 정렬 함수
    private List<Deal> sortDeals(List<Deal> deals, SortType sortType) {
        if (sortType == null) return deals;

        return switch (sortType) {
            case VIEWS -> deals.stream()
                    .sorted((d1, d2) -> Long.compare(getViewCount(d2), getViewCount(d1)))
                    .toList();

            case VOTES -> deals.stream()
                    .sorted((d1, d2) -> Long.compare(getVoteScore(d2), getVoteScore(d1)))
                    .toList();

            case LATEST -> deals.stream()
                    .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                    .toList();

            case DISCOUNT_RATE -> deals.stream()
                    .sorted((d1, d2) -> Double.compare(
                            getDiscountRate(d2),
                            getDiscountRate(d1)))
                    .toList();

            default -> deals;
        };
    }

    // 정렬 보조 함수
    private long getViewCount(Deal deal) {
        String key = "deal:view:" + deal.getDealId();
        Object val = redisTemplate.opsForValue().get(key);
        return val == null ? 0L : ((Number) val).longValue();
    }

    private long getVoteScore(Deal deal) {
        long like = voteRepository.countByDealIdAndVoteType(deal, VoteType.TRUE);
        long dislike = voteRepository.countByDealIdAndVoteType(deal, VoteType.FALSE);
        return like - dislike;
    }

    private double getDiscountRate(Deal deal) {
        double regular = deal.getPrice().regularPrice();
        double discounted = deal.getPrice().discountedPrice();
        return (regular > 0) ? (regular - discounted) / regular : 0;
    }


    // 시간 범위 정렬
    private LocalDateTime resolveStartDate(TimeRangeType timeRangeType) {
        if (timeRangeType == null) return null;

        LocalDateTime now = LocalDateTime.now();

        return switch (timeRangeType) {
            case LAST3HOURS -> now.minusHours(3);
            case LAST6HOURS -> now.minusHours(6);
            case LAST12HOURS -> now.minusHours(12);
            case LAST24HOURS -> now.minusHours(24);
            case LAST3DAYS -> now.minusDays(3);
            case LAST7DAYS -> now.minusDays(7);
        };
    }
}
