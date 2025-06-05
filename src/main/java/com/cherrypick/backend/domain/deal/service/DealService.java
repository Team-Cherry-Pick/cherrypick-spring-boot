package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.enums.PriceType;
import com.cherrypick.backend.domain.deal.enums.ShippingType;
import com.cherrypick.backend.domain.deal.enums.SortType;
import com.cherrypick.backend.domain.deal.enums.TimeRangeType;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.discount.entity.Discount;
import com.cherrypick.backend.domain.discount.repository.DiscountRepository;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ImageService imageService;
    private final ImageRepository imageRepository;

    // 게시글 생성
    @Transactional
    public DealResponseDTOs.Create createDeal(DealCreateRequestDTO dto) {

        // TODO: 딥링크 변환

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
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

        // 이미지랑 매핑
        if (dto.imageIds() != null && !dto.imageIds().isEmpty()) {
            imageService.attachImage(saved.getDealId(), dto.imageIds(), ImageType.DEAL);
        }

        return new DealResponseDTOs.Create(saved.getDealId(), "핫딜 게시글 생성 성공");
    }

    // 게시글 전체조회 (검색)
    public DealSearchPageResponseDTO searchDeals(DealSearchRequestDTO dto, int page, int size) {
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) {
            throw new BaseException(DealErrorCode.CATEGORY_NOT_FOUND);
        }

        if (dto.getDiscountIds() != null && !dto.getDiscountIds().isEmpty()) {
            List<Long> foundDiscountIds = discountRepository.findAllById(dto.getDiscountIds())
                    .stream().map(Discount::getDiscountId).toList();
            if (foundDiscountIds.size() != dto.getDiscountIds().size()) {
                throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
            }
        }

        if (dto.getStoreIds() != null && !dto.getStoreIds().isEmpty()) {
            List<Long> foundStoreIds = storeRepository.findAllById(dto.getStoreIds())
                    .stream().map(Store::getStoreId).toList();
            if (foundStoreIds.size() != dto.getStoreIds().size()) {
                throw new BaseException(DealErrorCode.STORE_NOT_FOUND);
            }
        }

        LocalDateTime startDate = resolveStartDate(dto.getTimeRange());
        LocalDateTime endDate = LocalDateTime.now();

        Double minPrice = null;
        Double maxPrice = null;
        List<PriceType> priceTypes = new ArrayList<>();

        if (dto.getPriceFilter() != null) {
            PriceType type = dto.getPriceFilter().priceType();

            minPrice = dto.getPriceFilter().minPrice();
            maxPrice = dto.getPriceFilter().maxPrice();

            if ((minPrice != null || maxPrice != null) && type == null) {
                throw new BaseException(DealErrorCode.INVALID_PRICE_TYPE);
            }

            if (type != null) priceTypes.add(type);
        }

        boolean viewSoldOut = dto.getFilters() != null && dto.getFilters().viewSoldOut();
        boolean freeShipping = dto.getFilters() != null && dto.getFilters().freeShipping();
        boolean variousPrice = dto.getVariousPrice() != null ? dto.getVariousPrice() : true;

        List<Long> discountIds = (dto.getDiscountIds() == null || dto.getDiscountIds().isEmpty()) ? null : dto.getDiscountIds();
        List<Long> storeIds = (dto.getStoreIds() == null || dto.getStoreIds().isEmpty()) ? null : dto.getStoreIds();

        boolean sortPriceHigh = dto.getSortType() == SortType.PRICE_HIGH;
        boolean sortPriceLow = dto.getSortType() == SortType.PRICE_LOW;

        Pageable pageable = PageRequest.of(page, size + 1);

        List<Deal> deals = dealRepository.searchDealsWithPaging(
                dto.getCategoryId(),
                dto.getKeyword(),
                viewSoldOut,
                freeShipping,
                startDate,
                endDate,
                minPrice,
                maxPrice,
                priceTypes.isEmpty() ? null : priceTypes,
                variousPrice,
                discountIds,
                storeIds,
                sortPriceHigh,
                sortPriceLow,
                pageable
        );

        boolean hasNext = deals.size() > size;
        if (hasNext) {
            deals = deals.subList(0, size);
        }

        List<Long> dealIds = deals.stream().map(Deal::getDealId).toList();

        List<String> redisKeys = dealIds.stream().map(id -> "deal:view:" + id).toList();
        List<Object> redisResults = redisTemplate.opsForValue().multiGet(redisKeys);
        Map<Long, Long> viewCountMap = new HashMap<>();
        for (int i = 0; i < dealIds.size(); i++) {
            Object val = redisResults.get(i);
            long viewCount = 0L;
            if (val != null) {
                try {
                    viewCount = Long.parseLong(val.toString());
                } catch (NumberFormatException ignored) {
                }
            }
            viewCountMap.put(dealIds.get(i), viewCount);
        }

        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(dealIds, VoteType.TRUE);
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(dealIds);
        Map<Long, Image> imageMap = imageRepository.findTopImagesByDealIds(dealIds, ImageType.DEAL).stream()
                .collect(Collectors.toMap(Image::getRefId, img -> img, (a, b) -> a));

        if (dto.getSortType() == SortType.POPULARITY
                || dto.getSortType() == SortType.VIEWS
                || dto.getSortType() == SortType.VOTES
                || dto.getSortType() == SortType.DISCOUNT_RATE) {
            deals = sortDeals(deals, dto.getSortType(), viewCountMap, likeCountMap);
        }

        List<DealSearchResponseDTO> responseList = deals.stream().map(deal -> {
            Long dealId = deal.getDealId();
            long viewCount = viewCountMap.getOrDefault(dealId, 0L);
            long likeCount = likeCountMap.getOrDefault(dealId, 0L);
            long commentCount = commentCountMap.getOrDefault(dealId, 0L);

            Image firstImage = imageMap.get(dealId);
            ImageUrl imageUrl = null;
            if (firstImage != null) {
                imageUrl = new ImageUrl(
                        firstImage.getImageId(),
                        firstImage.getImageUrl(),
                        firstImage.getImageIndex()
                );
            }

            return new DealSearchResponseDTO(
                    dealId,
                    imageUrl,
                    deal.getTitle(),
                    deal.getStoreId() != null ? deal.getStoreId().getName() : deal.getStoreName(),
                    getInfoTags(deal),
                    deal.getPrice(),
                    deal.getCreatedAt().toString(),
                    (int) deal.getDealScore(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();

        return new DealSearchPageResponseDTO(responseList, hasNext);
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
            System.out.println("Current categoryId: " + categoryId);

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));

            categoryNames.add(0, category.getName());  // 부모부터 자식 순으로 저장하기 위해 앞에 추가
            categoryId = category.getParentId();  // 부모 카테고리로 이동
        }

        // 이미지 조회
        Optional<Image> userImageOpt = imageRepository.findByUserId(
                deal.getUserId().getUserId()
        );

        // User VO 생성
        com.cherrypick.backend.domain.user.vo.User userVo = new com.cherrypick.backend.domain.user.vo.User(
                deal.getUserId().getUserId(),
                deal.getUserId().getNickname(),
                userImageOpt.map(Image::getImageUrl).orElse(null)
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

        long totalViews = 0L;
        if (redisVal != null) {
            try {
                totalViews = Long.parseLong(redisVal.toString());
            } catch (NumberFormatException e) {
                totalViews = 0L; // 파싱 실패하면 안전하게 0으로 처리
            }
        }

        // 매트릭스 조회 (조회수, 좋아요 수, 싫어요 수, 댓글 수)
        long[] metrics = getDealMetrics(deal);

        List<Image> images = imageRepository.findByRefIdAndImageTypeOrderByImageIndexAsc(deal.getDealId(), ImageType.DEAL);

        List<ImageUrl> imageUrls = images.stream()
                .map(image -> new ImageUrl(
                        image.getImageId(),
                        image.getImageUrl(),
                        image.getImageIndex()
                ))
                .toList();

        return new DealDetailResponseDTO(
                deal.getDealId(),
                imageUrls,
                userVo,
                storeVo,
                categoryNames,
                deal.getTitle(),
                infoTags,
                deal.getShipping(),
                deal.getPrice(),
                deal.getContent(),
                (int) deal.getDealScore(),
                (int) totalViews,
                (int) metrics[0], // likeCount
                (int) metrics[1], // dislikeCount
                (int) metrics[2], // commentCount
                deal.getDeepLink(),
                deal.getOriginalUrl(),
                deal.isSoldOut()
        );
    }

    // 게시글 수정
    @Transactional
    public DealResponseDTOs.Update updateDeal(DealUpdateRequestDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
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

        // 이미지 매핑
        if (dto.imageUrls() != null && !dto.imageUrls().isEmpty()) {
            imageService.attachAndIndexImages(deal.getDealId(), dto.imageUrls(), ImageType.DEAL);
        }

        return new DealResponseDTOs.Update(deal.getDealId(), "핫딜 게시글 수정 성공");
    }

    // 게시글 삭제 (Soft Delete)
    @Transactional
    public DealResponseDTOs.Delete deleteDeal(Long dealId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 작성자 검증
        if (!deal.getUserId().getUserId().equals(userDetails.userId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        deal.setIsDelete(true);

        // 삭제 후 해당 이미지 isTemp = true로 설정
        List<Image> images = imageRepository.findByRefIdAndImageTypeOrderByImageIndexAsc(dealId, ImageType.DEAL);
        for (Image image : images) {
            image.setTemp(true);
        }

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
    public static List<String> getInfoTags(Deal deal) {
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
    private List<Deal> sortDeals(List<Deal> deals, SortType sortType,
                                 Map<Long, Long> viewCountMap,
                                 Map<Long, Long> likeCountMap) {
        if (sortType == null) return deals;

        return switch (sortType) {
            case VIEWS -> deals.stream()
                    .sorted((d1, d2) -> Long.compare(
                            viewCountMap.getOrDefault(d2.getDealId(), 0L),
                            viewCountMap.getOrDefault(d1.getDealId(), 0L)))
                    .toList();

            case VOTES -> deals.stream()
                    .sorted((d1, d2) -> Long.compare(
                            likeCountMap.getOrDefault(d2.getDealId(), 0L),
                            likeCountMap.getOrDefault(d1.getDealId(), 0L)))
                    .toList();

            case POPULARITY -> deals.stream()
                    .sorted((d1, d2) -> Double.compare(d2.getDealScore(), d1.getDealScore()))
                    .toList();

            case DISCOUNT_RATE -> deals.stream()
                    .sorted((d1, d2) -> Double.compare(
                            getDiscountRate(d2),
                            getDiscountRate(d1)))
                    .toList();

            case LATEST -> deals.stream()
                    .sorted((d1, d2) -> d2.getCreatedAt().compareTo(d1.getCreatedAt()))
                    .toList();

            default -> deals;
        };
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
