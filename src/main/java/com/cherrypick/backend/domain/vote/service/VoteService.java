package com.cherrypick.backend.domain.vote.service;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.user.dto.UserDetailDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.vote.dto.request.VoteRequestDTO;
import com.cherrypick.backend.domain.vote.dto.response.VoteResponseDTO;
import com.cherrypick.backend.domain.vote.entity.Vote;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import com.cherrypick.backend.domain.vote.repository.VoteRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.VoteErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;

    @Transactional
    public VoteResponseDTO vote(Long dealId, VoteRequestDTO request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 비추천일 경우만 사유 저장
        String content = null;
        if (request.voteType() == VoteType.FALSE && request.dislikeReason() != null) {
            content = request.dislikeReason().name();
        }

        // 비추천 사유가 없으면 에러 발생
        if (request.voteType() == VoteType.FALSE && request.dislikeReason() == null) {
            throw new BaseException(VoteErrorCode.DISLIKE_REASON_REQUIRED);
        }

        // 추천 또는 무투표 상태에서 사유가 있으면 에러 발생
        if ((request.voteType() == VoteType.TRUE || request.voteType() == VoteType.NONE) && request.dislikeReason() != null) {
            throw new BaseException(VoteErrorCode.DISLIKE_REASON_INVALID);
        }

        // 기존 투표 여부 확인
        Vote vote = voteRepository.findByUserIdAndDealId(user, deal)
                .orElse(new Vote());

        vote.setUserId(user);
        vote.setDealId(deal);
        vote.setVoteType(request.voteType());
        vote.setContent(content);

        voteRepository.save(vote);

        return new VoteResponseDTO(deal.getDealId(), "핫딜 게시글 투표 성공");
    }
}
