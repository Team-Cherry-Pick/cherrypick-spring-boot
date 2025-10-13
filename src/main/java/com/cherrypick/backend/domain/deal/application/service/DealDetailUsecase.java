package com.cherrypick.backend.domain.deal.application.service;

import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.adapter.out.RedisDuplicationPreventionAdapter;
import com.cherrypick.backend.domain.deal.application.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.entity.Discount;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.domain.service.DealValidationService;
import com.cherrypick.backend.domain.deal.domain.service.reference.CategoryService;
import com.cherrypick.backend.domain.deal.domain.service.reference.StoreService;
import com.cherrypick.backend.domain.deal.util.InfoTagGenerator;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.user.service.UserService;
import com.cherrypick.backend.domain.user.vo.AuthorDTO;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.service.VoteService;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.cherrypick.backend.domain.deal.adapter.out.RedisDuplicationPreventionAdapter.Behavior;

@Service
@RequiredArgsConstructor
public class DealDetailUsecase {

    // 의존성
    private final DealValidationService dealValidationService;
    private final VoteService voteService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final StoreService storeService;
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final RedisDuplicationPreventionAdapter duplicationAdapter;
    private final DealRepository dealRepository;

    @Transactional
    public DealDetailResponseDTO getDealDetail(Long dealId, String deviceId) {

        // 1. Deal 유효성 검증 및 조회 ✅
        Deal deal = dealValidationService.getValidatedDeal(dealId);

        // 2. 조회수/온도 증가 (중복 방지)
        incrementViewIfNeeded(dealId, deviceId);

        // 3. 로그인 사용자 ID 추출
        Long userId = AuthUtil.isAuthenticated() ? AuthUtil.getUserDetail().userId() : null;

        // 4. Vote 불러오기 ✅
        VoteType voteType = voteService.getUserVote(deal, userId);

        // 5. 카테고리 계층 불러오기 ✅
        Long finalCategoryId = deal.getCategory().getCategoryId();
        List<String> categoryNames = categoryService.getCategoryHierarchy(finalCategoryId);

        // 6. 게시자 정보 불러오기
        AuthorDTO authorDTO = AuthorDTO.from(deal.getUser(), null);

        // 7. 스토어 정보 불러오기
        DealDetailResponseDTO.StoreVO storeVO = storeService.getStoreInfo(deal);

        // 8. 이미지 불러오기
        List<ImageUrl> imageUrls = imageService.getImages(dealId);

        // 9. 할인정보 불러오기
        List<Long> discountIds = getDiscountIds(deal);

        // 10. 메트릭스 불러오기 (좋아요/싫어요/댓글 수)
        long likeCount = deal.getVotes().stream()
                .filter(v -> v.getVoteType() == VoteType.TRUE)
                .count();
        long dislikeCount = deal.getVotes().stream()
                .filter(v -> v.getVoteType() == VoteType.FALSE)
                .count();
        long commentCount = commentRepository.countByDealId_DealIdAndIsDeleteFalse(deal.getDealId());

        // 11. 인포 태그 생성
        List<String> infoTags = InfoTagGenerator.getInfoTags(deal);

        // 12. DTO 조합 반환
        return new DealDetailResponseDTO(
                deal.getDealId(),
                imageUrls,
                authorDTO,
                storeVO,
                categoryNames,
                deal.getTitle(),
                infoTags,
                deal.getShippingType(),
                deal.getPrice(),
                deal.getContent(),
                deal.getDiscountDescription(),
                deal.getHeat(),
                deal.getTotalViews(),
                (int) likeCount,
                (int) dislikeCount,
                (int) commentCount,
                deal.getOriginalUrl(),
                deal.getDeepLink(),
                deal.isSoldOut(),
                voteType,
                finalCategoryId,
                deal.getStore() != null ? deal.getStore().getStoreId() : null,
                discountIds,
                deal.getDiscountName()
        );
    }

    /**
     * Deal에 연결된 할인 정보 ID 목록 조회
     *
     * @param deal 딜 엔티티
     * @return 할인 ID 목록 (할인이 없는 경우 빈 리스트)
     */
    private List<Long> getDiscountIds(Deal deal) {
        if (deal.getDiscounts() == null || deal.getDiscounts().isEmpty()) {
            return List.of();
        }

        return deal.getDiscounts().stream()
                .map(Discount::getDiscountId)
                .toList();
    }

    /**
     * 조회수 및 온도 증가 (중복 방지)
     * Redis를 통해 24시간 내 중복 조회 방지
     *
     * @param dealId 딜 ID
     * @param deviceId 디바이스 ID
     */
    private void incrementViewIfNeeded(Long dealId, String deviceId) {
        if (duplicationAdapter.isDuplicate(Behavior.VIEW, dealId, deviceId)) {
            return; // 이미 조회한 경우 증가하지 않음
        }

        dealRepository.incrementViewCount(dealId);
        dealRepository.updateHeat(dealId, +0.1);
        duplicationAdapter.preventDuplicate(Behavior.VIEW, dealId, deviceId);
    }

}
