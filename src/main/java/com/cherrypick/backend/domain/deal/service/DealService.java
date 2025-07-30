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
import com.cherrypick.backend.domain.deal.enums.*;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.discount.entity.Discount;
import com.cherrypick.backend.domain.discount.repository.DiscountRepository;
import com.cherrypick.backend.domain.hashtag.service.HashTagService;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.linkprice.service.LinkPriceService;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.ImageErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final HashTagService  hashTagService;
    private final RecommenderService recommenderService;
    private final LinkPriceService linkPriceService;

    // 게시글 생성
    @Transactional
    public DealResponseDTOs.Create createDeal(DealCreateRequestDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticatedUser userDetails)) {
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

        // 딥링크 변환
        String deepLink = linkPriceService.createDeeplink(dto.originalUrl());

        Deal deal = Deal.builder()
                .userId(user)
                .title(dto.title())
                .categoryId(category)
                .originalUrl(dto.originalUrl())
                .deepLink(deepLink)
                .storeId(store)
                .storeName(store == null ? dto.storeName() : null)
                .price(dto.price())
                .shipping(dto.shipping())
                .content(dto.content())
                .discounts(discounts)
                .discountName(discountName)
                .discountDescription(dto.discountDescription())
                .isSoldOut(false)
                .build();

        Deal saved = dealRepository.save(deal);

        // 해쉬태그 생성
        hashTagService.saveHashTags(saved.getDealId(), hashTagService.getChatGPTResponse(saved.getTitle(), saved.getContent()));

        // 이미지랑 매핑
        if (dto.imageIds() != null && !dto.imageIds().isEmpty()) {
            imageService.attachImage(saved.getDealId(), dto.imageIds(), ImageType.DEAL);
        }

        return new DealResponseDTOs.Create(saved.getDealId(), "핫딜 게시글 생성 성공");
    }

    // 게시글 전체조회 (검색)
    public DealSearchPageResponseDTO searchDeals(DealSearchRequestDTO dto, int page, int size) {
        // 카테고리 유효성
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) {
            throw new BaseException(DealErrorCode.CATEGORY_NOT_FOUND);
        }

        // 할인 ID 유효성 및 존재 여부
        if (dto.getDiscountIds() != null && !dto.getDiscountIds().isEmpty()) {
            for (Long discountId : dto.getDiscountIds()) {
                if (discountId == null || discountId <= 0) {
                    throw new BaseException(DealErrorCode.INVALID_DISCOUNT_INFORMATION);
                }
            }
            List<Long> foundDiscountIds = discountRepository.findAllById(dto.getDiscountIds())
                    .stream()
                    .map(Discount::getDiscountId)
                    .toList();
            if (foundDiscountIds.size() != dto.getDiscountIds().size()) {
                throw new BaseException(DealErrorCode.DISCOUNT_NOT_FOUND);
            }
        }

        // 스토어 ID 존재 여부
        if (dto.getStoreIds() != null && !dto.getStoreIds().isEmpty()) {
            List<Long> foundStoreIds = storeRepository.findAllById(dto.getStoreIds())
                    .stream()
                    .map(Store::getStoreId)
                    .toList();
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

        Sort sort;
        switch (dto.getSortType()) {
            case PRICE_HIGH -> sort = Sort.by(Sort.Direction.DESC, "price.discountedPrice");
            case PRICE_LOW -> sort = Sort.by(Sort.Direction.ASC, "price.discountedPrice");
            case LATEST -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            default -> sort = Sort.unsorted();
        }

        List<Deal> allFilteredDeals = dealRepository.searchDealsWithPaging(
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
                PageRequest.of(0, Integer.MAX_VALUE, sort)
        );

        List<Long> allDealIds = allFilteredDeals.stream().map(Deal::getDealId).toList();

        // 조회수 가져오는 부분
        Map<Long, Long> viewCountMap = allFilteredDeals.stream()
                .collect(Collectors.toMap(
                        Deal::getDealId,
                        deal -> deal.getTotalViews() != null ? deal.getTotalViews() : 0L
                ));

        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(allDealIds, VoteType.TRUE);
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(allDealIds);

        if (dto.getSortType() == SortType.POPULARITY
                || dto.getSortType() == SortType.VIEWS
                || dto.getSortType() == SortType.DISCOUNT_RATE) {
            allFilteredDeals = sortDeals(allFilteredDeals, dto.getSortType(), viewCountMap, likeCountMap);
        }

        // 정렬 후 페이징 적용
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allFilteredDeals.size());
        List<Deal> pageContent = fromIndex >= allFilteredDeals.size() ? List.of() : allFilteredDeals.subList(fromIndex, toIndex);
        boolean hasNext = toIndex < allFilteredDeals.size();

        List<Long> pageDealIds = pageContent.stream().map(Deal::getDealId).toList();

        // 카테고리 정보 조회 (N+1 방지)
        Map<Long, Category> categoryMap = new HashMap<>();
        if (!pageContent.isEmpty()) {
            List<Long> categoryIds = pageContent.stream()
                    .map(deal -> deal.getCategoryId().getCategoryId())
                    .distinct()
                    .toList();
            categoryRepository.findAllById(categoryIds).forEach(category ->
                    categoryMap.put(category.getCategoryId(), category));
        }

        // 스토어 정보 조회 (N+1 방지)
        Map<Long, Store> storeMap = new HashMap<>();
        if (!pageContent.isEmpty()) {
            List<Long> storeIdsFromDeals = pageContent.stream()
                    .map(deal -> deal.getStoreId())
                    .filter(store -> store != null)
                    .map(Store::getStoreId)
                    .distinct()
                    .toList();
            if (!storeIdsFromDeals.isEmpty()) {
                storeRepository.findAllById(storeIdsFromDeals).forEach(store ->
                        storeMap.put(store.getStoreId(), store));
            }
        }

        // 사용자 정보 조회 (N+1 방지)
        Map<Long, User> userMap = new HashMap<>();
        if (!pageContent.isEmpty()) {
            List<Long> userIds = pageContent.stream()
                    .map(deal -> deal.getUserId().getUserId())
                    .distinct()
                    .toList();
            userRepository.findAllById(userIds).forEach(user ->
                    userMap.put(user.getUserId(), user));
        }

        Map<Long, Image> imageMap = imageRepository.findTopImagesByDealIds(pageDealIds, ImageType.DEAL).stream()
                .collect(Collectors.toMap(Image::getRefId, img -> img, (a, b) -> a));

        List<DealSearchResponseDTO> responseList = pageContent.stream().map(deal -> {
            Long dealId = deal.getDealId();
            long likeCount = likeCountMap.getOrDefault(dealId, 0L);
            long commentCount = commentCountMap.getOrDefault(dealId, 0L);
            Image image = imageMap.get(dealId);

            User user = userMap.get(deal.getUserId().getUserId());
            Store store = deal.getStoreId() != null ? storeMap.get(deal.getStoreId().getStoreId()) : null;

            return new DealSearchResponseDTO(
                    dealId,
                    image != null ? new ImageUrl(image.getImageId(), image.getImageUrl(), image.getImageIndex()) : null,
                    deal.getTitle(),
                    store != null ? store.getName() : deal.getStoreName(),
                    getInfoTags(deal),
                    deal.getPrice(),
                    user != null ? user.getNickname() : null,
                    deal.getCreatedAt().toString(),
                    (int) deal.getHeat(),
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

        // 로그인 사용자 ID 추출
        final Long loginUserId = com.cherrypick.backend.global.util.AuthUtil.isAuthenticated() ? com.cherrypick.backend.global.util.AuthUtil.getUserDetail().userId() : null;
        com.cherrypick.backend.domain.vote.enums.VoteType voteType = com.cherrypick.backend.domain.vote.enums.VoteType.NONE;
        if (loginUserId != null) {
            var user = userRepository.findById(loginUserId).orElse(null);
            if (user != null) {
                var voteOpt = voteRepository.findByUserIdAndDealId(user, deal);
                if (voteOpt.isPresent()) {
                    voteType = voteOpt.get().getVoteType();
                }
            }
        }

        // 카테고리 이름 부모→자식 순으로
        List<String> categoryNames = new ArrayList<>();
        Long currentCategoryId = deal.getCategoryId() != null ? deal.getCategoryId().getCategoryId() : null;
        Long finalCategoryId = currentCategoryId; // 최종 카테고리 아이디

        while (currentCategoryId != null) {
            Category category = categoryRepository.findById(currentCategoryId)
                    .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));
            categoryNames.add(0, category.getName());
            currentCategoryId = category.getParentId();
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

        // Store VO 생성
        com.cherrypick.backend.domain.store.vo.Store storeVo;
        Long storeId = null;
        String storeName = null;
        if (deal.getStoreId() != null) {
            storeId = deal.getStoreId().getStoreId();
            storeName = deal.getStoreId().getName();
            storeVo = new com.cherrypick.backend.domain.store.vo.Store(
                    deal.getStoreId().getName(),
                    deal.getStoreId().getTextColor(),
                    deal.getStoreId().getBackgroundColor()
            );
        } else {
            storeName = deal.getStoreName();
            storeVo = new com.cherrypick.backend.domain.store.vo.Store(
                    deal.getStoreName(),
                    null,
                    null
            );
        }

        // 인포 태그 생성
        List<String> infoTags = getInfoTags(deal);

        // 조회수 증가
        deal.setTotalViews(deal.getTotalViews() + 1);
        long totalViews = deal.getTotalViews();

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

        // 할인 정보 처리
        List<Long> discountIds = new ArrayList<>();
        if (deal.getDiscounts() != null && !deal.getDiscounts().isEmpty()) {
            discountIds = deal.getDiscounts().stream()
                    .map(Discount::getDiscountId)
                    .toList();
        }

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
                deal.getDiscountDescription(),
                (int) deal.getHeat(),
                (int) totalViews,
                (int) metrics[0], // likeCount
                (int) metrics[1], // dislikeCount
                (int) metrics[2], // commentCount
                deal.getDeepLink(),
                deal.getOriginalUrl(),
                deal.isSoldOut(),
                voteType,
                finalCategoryId,
                storeId,
                discountIds,
                deal.getDiscountName()
        );
    }

    // 게시글 수정
    @Transactional
    public DealResponseDTOs.Update updateDeal(DealUpdateRequestDTO dto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticatedUser userDetails)) {
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

        Store store;
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

        if (dto.discountDescription() != null) {
            deal.setDiscountDescription(dto.discountDescription());
        }

        if (dto.isSoldOut()) {
            deal.setSoldOut(true); // dto.isSoldOut()이 true일 경우
        } else {
            deal.setSoldOut(false); // dto.isSoldOut()이 false일 경우
        }

        // 이미지 매핑
        if (dto.imageIds() != null) {
            // 1. 기존 이 게시글에 연결된 이미지들 가져오기
            List<Image> existingImages = imageRepository.findByRefIdAndImageTypeOrderByImageIndexAsc(deal.getDealId(), ImageType.DEAL);

            // 2. 요청으로 온 이미지 ID들을 Set으로 만들어서 비교
            List<Long> requestedImageIds = dto.imageIds();
            Set<Long> requestedSet = new HashSet<>(requestedImageIds);

            // 3. 기존 이미지 중 요청에 포함되지 않은 것들은 isTemp = true
            for (Image img : existingImages) {
                if (!requestedSet.contains(img.getImageId())) {
                    img.setTemp(true);
                }
            }

            // 4. 요청으로 온 이미지 ID 순서대로 index 매기고 isTemp=false, refId 세팅
            int index = 0;
            for (Long imgId : requestedImageIds) {
                Image img = imageRepository.findById(imgId)
                        .orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND));
                img.setRefId(deal.getDealId());
                img.setImageIndex(index++);
                img.setTemp(false);
            }
        }

        return new DealResponseDTOs.Update(deal.getDealId(), "핫딜 게시글 수정 성공");
    }

    // 게시글 삭제 (Soft Delete)
    @Transactional
    public DealResponseDTOs.Delete deleteDeal(Long dealId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticatedUser userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 작성자 또는 관리자 검증
        boolean isOwner = deal.getUserId().getUserId().equals(userDetails.userId());
        boolean isAdmin = userDetails.role() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        // 삭제 처리
        deal.setIsDelete(true);

        // 관련 이미지 isTemp=true로 변경
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
    private List<Deal> sortDeals(List<Deal> deals, SortType sortType, Map<Long, Long> viewCountMap, Map<Long, Long> likeCountMap) {
        Comparator<Deal> comparator = null;

        switch (sortType) {
            case POPULARITY:
                comparator = Comparator.comparingDouble(Deal::getHeat).reversed()
                        .thenComparing(Deal::getCreatedAt, Comparator.reverseOrder());
                break;

            case VIEWS:
                comparator = Comparator.<Deal>comparingLong(d -> viewCountMap.getOrDefault(d.getDealId(), 0L)).reversed()
                        .thenComparing(Deal::getCreatedAt, Comparator.reverseOrder());
                break;

            case DISCOUNT_RATE:
                comparator = Comparator.comparingDouble(this::getDiscountRate).reversed()
                        .thenComparing(Deal::getCreatedAt, Comparator.reverseOrder());
                break;

            default:
                return deals;
        }

        return deals.stream().sorted(comparator).toList();
    }

    // 할인률 계산 함수
    private double getDiscountRate(Deal deal) {
        if (deal.getPrice() == null) return 0.0;
        Double regular = deal.getPrice().regularPrice();
        Double discounted = deal.getPrice().discountedPrice();
        if (regular == null || discounted == null || regular <= 0) return 0.0;

        return (regular - discounted) / regular;
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