package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.util.InfoTagGenerator;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Deal 엔티티를 검색 결과 DTO로 변환하는 팩토리 서비스
 *
 * <p>검색된 Deal 리스트에 연관 데이터(좋아요, 댓글, 이미지)를 조회하여
 * 응답 DTO로 변환합니다. User, Store, Category는 fetchJoin으로 이미 로드되어 있습니다.</p>
 */
@Service @RequiredArgsConstructor
public class DealSearchResponseFactory
{
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;

    /**
     * Deal 리스트에 연관 데이터를 로드하여 검색 응답 DTO로 변환
     *
     * @param deals fetchJoin으로 User, Store, Category가 로드된 Deal 리스트
     * @param hasNext 다음 페이지 존재 여부
     * @return 검색 결과 페이지 응답 DTO
     */
    public DealSearchPageResponseDTO loadRelations(List<Deal> deals, boolean hasNext)
    {
        if (deals.isEmpty()) {
            return new DealSearchPageResponseDTO(List.of(), false);
        }

        List<Long> dealIds = deals.stream().map(Deal::getDealId).toList();

        // 좋아요 수 일괄 조회 (IN 쿼리)
        Map<Long, Long> likeCountMap = voteRepository.countByDealIdsAndVoteTypeGrouped(dealIds, VoteType.TRUE);

        // 댓글 수 일괄 조회 (IN 쿼리)
        Map<Long, Long> commentCountMap = commentRepository.countByDealIdsGrouped(dealIds);

        // 대표 이미지 일괄 조회 (IN 쿼리)
        Map<Long, Image> imageMap = imageRepository.findTopImagesByDealIds(dealIds, ImageType.DEAL).stream()
                .collect(Collectors.toMap(Image::getRefId, img -> img, (a, b) -> a));

        // Deal → DTO 변환
        List<DealSearchResponseDTO> responseList = deals.stream().map(deal -> {
            Long dealId = deal.getDealId();
            long likeCount = likeCountMap.getOrDefault(dealId, 0L);
            long commentCount = commentCountMap.getOrDefault(dealId, 0L);
            Image image = imageMap.get(dealId);

            // fetchJoin으로 이미 로드된 데이터 사용 (추가 쿼리 없음)
            User user = deal.getUser();
            Store store = deal.getStore();

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
                    deal.getHeat(),
                    (int) likeCount,
                    (int) commentCount,
                    deal.isSoldOut()
            );
        }).toList();

        return new DealSearchPageResponseDTO(responseList, hasNext);
    }

}