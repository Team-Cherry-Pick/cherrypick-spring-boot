package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.repository.DealRepository;
import com.cherrypick.backend.domain.deal.util.InfoTagGenerator;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class DealEnrichmentService
{
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public DealSearchPageResponseDTO loadRelations(List<Deal> deals, int page, int size
    )
    {
        List<Long> allDealIds = deals.stream().map(Deal::getDealId).toList();

        // 조회수 가져오는 부분
        Map<Long, Long> viewCountMap = deals.stream()
                .collect(Collectors.toMap(
                        Deal::getDealId,
                        deal -> deal.getTotalViews() != null ? deal.getTotalViews() : 0L
                ));

        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(allDealIds, VoteType.TRUE);
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(allDealIds);

        // 정렬 후 페이징 적용
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, deals.size());
        List<Deal> pageContent = fromIndex >= deals.size() ? List.of() : deals.subList(fromIndex, toIndex);
        boolean hasNext = toIndex < deals.size();

        List<Long> pageDealIds = pageContent.stream().map(Deal::getDealId).toList();

        // 카테고리 정보 조회 (N+1 방지)
        Map<Long, Category> categoryMap = new HashMap<>();
        if (!pageContent.isEmpty()) {
            List<Long> categoryIds = pageContent.stream()
                    .map(deal -> deal.getCategory().getCategoryId())
                    .distinct()
                    .toList();
            categoryRepository.findAllById(categoryIds).forEach(category ->
                    categoryMap.put(category.getCategoryId(), category));
        }

        // 스토어 정보 조회 (N+1 방지)
        Map<Long, Store> storeMap = new HashMap<>();
        if (!pageContent.isEmpty()) {
            List<Long> storeIdsFromDeals = pageContent.stream()
                    .map(Deal::getStore)
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
                    .map(deal -> deal.getUser().getUserId())
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

            User user = userMap.get(deal.getUser().getUserId());
            Store store = deal.getStore() != null ? storeMap.get(deal.getStore().getStoreId()) : null;

            return new DealSearchResponseDTO(
                    dealId,
                    image != null ? new ImageUrl(image.getImageId(), image.getImageUrl(), image.getImageIndex()) : null,
                    deal.getTitle(),
                    store != null ? store.getName() : deal.getStoreName(),
                    InfoTagGenerator.getInfoTags(deal),
                    deal.getPrice(),
                    user != null ? user.getNickname() : null,
                    user != null ? user.getBadge().getBadgeId() : null,
                    deal.getCreatedAt().toString(),
                    (int) deal.getHeat(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();

        return new DealSearchPageResponseDTO(responseList, hasNext);
    }

}
