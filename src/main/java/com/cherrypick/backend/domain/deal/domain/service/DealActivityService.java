package com.cherrypick.backend.domain.deal.domain.service;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.comment.service.CommentService;
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
import com.cherrypick.backend.domain.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class DealActivityService
{
    private final DealRepository dealRepository;
    private final DealEnrichmentService dealEnrichmentService;

    /**
     * 특정 사용자가 작성한 모든 딜을 조회합니다.
     *
     * <p>삭제된 딜을 포함하여 해당 사용자가 작성한 모든 딜을 반환합니다.
     * 연관 데이터(카테고리, 스토어 등)는 지연 로딩되므로, 필요시 별도 처리가 필요합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 작성한 딜 목록 (빈 리스트 가능)
     */
    public List<Deal> getDealsByAuthor(Long userId)
    {
        List<Deal> deals = dealRepository.findDealsByUserId(userId);
        return deals;
    }

    /**
     * 특정 사용자가 좋아요(추천)를 누른 모든 딜을 조회합니다.
     *
     * <p>Vote 테이블과 조인하여 VoteType.TRUE인 딜만 반환합니다.
     * 연관 데이터(카테고리, 스토어 등)는 지연 로딩되므로, 필요시 별도 처리가 필요합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 좋아요를 누른 딜 목록 (빈 리스트 가능)
     */
    public List<Deal> getLikedDealsByUserId(Long userId)
    {
        List<Deal> deals = dealRepository.findLikedDealsByUserId(userId);
        return deals;
    }

    /**
     * 특정 사용자가 댓글을 작성한 모든 딜을 조회합니다.
     *
     * <p>Comment 테이블과 조인하여 해당 사용자가 댓글을 단 딜을 반환합니다.
     * 연관 데이터(카테고리, 스토어 등)는 지연 로딩되므로, 필요시 별도 처리가 필요합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 댓글을 작성한 딜 목록 (중복 제거, 빈 리스트 가능)
     */
    public List<Deal> getCommentedDealsByUserId(Long userId)
    {
        List<Deal> deals = dealRepository.findCommentedDealsByUserId(userId);
        return deals;
    }


}
