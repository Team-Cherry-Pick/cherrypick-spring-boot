package com.cherrypick.backend.domain.deal.application.service;

import com.cherrypick.backend.domain.deal.domain.repository.reference.CategoryRepository;
import com.cherrypick.backend.domain.deal.domain.entity.Category;
import com.cherrypick.backend.domain.deal.domain.service.reference.CategoryService;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.application.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.enums.SortType;
import com.cherrypick.backend.domain.deal.domain.enums.TimeRangeType;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.util.InfoTagGenerator;
import com.cherrypick.backend.domain.deal.domain.entity.Discount;
import com.cherrypick.backend.domain.deal.domain.repository.reference.DiscountRepository;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.linkprice.service.LinkPriceService;
import com.cherrypick.backend.domain.deal.domain.entity.Store;
import com.cherrypick.backend.domain.deal.domain.repository.reference.StoreRepository;
import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.deal.adapter.out.RedisDuplicationPreventionAdapter;
import static com.cherrypick.backend.domain.deal.adapter.out.RedisDuplicationPreventionAdapter.Behavior;

import com.cherrypick.backend.domain.user.entity.Badge;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.user.vo.AuthorDTO;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.ImageErrorCode;
import com.cherrypick.backend.global.exception.enums.LinkPriceErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;

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
    private final LinkPriceService linkPriceService;
    private final CategoryService categoryService;
    private final RedisDuplicationPreventionAdapter duplicationPreventionAdapter;


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
        if (!deal.getUser().getUserId().equals(userDetails.userId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        if (dto.title() != null) {
            deal.setTitle(dto.title());
        }

        if (dto.categoryId() != null) {
            Category category = categoryRepository.findById(dto.categoryId())
                    .orElseThrow(() -> new BaseException(DealErrorCode.CATEGORY_NOT_FOUND));
            deal.setCategory(category);
        }

        if (dto.originalUrl() != null) {
            if (!isValidUrl(dto.originalUrl())) {
                throw new BaseException(LinkPriceErrorCode.INVALID_ORIGINAL_URL);
            }

            deal.setOriginalUrl(dto.originalUrl());

            // 딥링크 재생성
            try {
                String deepLink = linkPriceService.createDeeplink(dto.originalUrl());
                deal.setDeepLink(deepLink);
            } catch (BaseException e) {
                if (e.getErrorCode() == LinkPriceErrorCode.LINKPRICE_API_RESULT_FAIL) {
                    deal.setDeepLink(null); // 딥링크 생성 실패 시 null로 저장
                } else {
                    throw e; // 그 외 예외는 그대로 전파
                }
            }
        }

        Store store;
        if (dto.storeId() != null) {
            store = storeRepository.findById(dto.storeId())
                    .orElseThrow(() -> new BaseException(DealErrorCode.STORE_NOT_FOUND));
            deal.setStore(store);
            deal.setStoreName(null); // storeId 있으면 storeName은 무시
        } else if (dto.storeName() != null) {
            deal.setStore(null);
            deal.setStoreName(dto.storeName());
        }

        if (dto.price() != null) {
            deal.setPrice(dto.price());
        }

        if (dto.shippingType() != null) {
            deal.setShippingType(dto.shippingType());
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
        boolean isOwner = deal.getUser().getUserId().equals(userDetails.userId());
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
        Double regular = deal.getPrice().getRegularPrice();
        Double discounted = deal.getPrice().getDiscountedPrice();
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

    // URL 검증
    public static boolean isValidUrl(String url) {
        Set<String> blockedShortDomains = Set.of(
                "bit.ly", "t.co", "goo.gl", "tinyurl.com", "is.gd",
                "ow.ly", "buff.ly", "cutt.ly", "rebrand.ly", "shorturl.at",
                "adf.ly", "lnkd.in"
        );

        if (url == null || url.isBlank()) return false;

        try {
            URI uri = new URI(url);

            // http 또는 https만 허용
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            // 호스트 검증
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            // 단축 URL 도메인 차단
            if (blockedShortDomains.contains(host.toLowerCase())) {
                return false;
            }

            return true;

        } catch (URISyntaxException e) {
            return false;
        }
    }

}