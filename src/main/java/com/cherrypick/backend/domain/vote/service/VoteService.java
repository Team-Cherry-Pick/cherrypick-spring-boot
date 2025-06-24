package com.cherrypick.backend.domain.vote.service;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.deal.service.RecommenderService;
import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
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

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final RecommenderService recommenderService;

    @Transactional
    public VoteResponseDTO createVote(Long dealId, VoteRequestDTO request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        String content = null;
        if (request.voteType() == VoteType.FALSE && request.dislikeReason() != null) {
            content = request.dislikeReason().name();
        }
        if (request.voteType() == VoteType.FALSE && request.dislikeReason() == null) {
            throw new BaseException(VoteErrorCode.DISLIKE_REASON_REQUIRED);
        }
        if ((request.voteType() == VoteType.TRUE || request.voteType() == VoteType.NONE) && request.dislikeReason() != null) {
            throw new BaseException(VoteErrorCode.DISLIKE_REASON_INVALID);
        }

        // 기존 투표 조회
        Vote vote = voteRepository.findByUserIdAndDealId(user, deal)
                .orElse(new Vote());

        // 이전 점수 제거
        double prevScore = vote.getScore();
        deal.setHeat(clampScore(deal.getHeat() - prevScore));

        // 새 점수 계산
        double newScore = 0.0;
        if (request.voteType() != VoteType.NONE) {
            newScore = calculateSingleScore(user, deal, request.voteType(), LocalDateTime.now());
            deal.setHeat(clampScore(deal.getHeat() + newScore));
        }

        // 투표 갱신
        vote.setUserId(user);
        vote.setDealId(deal);
        vote.setVoteType(request.voteType());
        vote.setContent(content);
        vote.setScore(newScore);
        voteRepository.save(vote);

        dealRepository.save(deal);

        return new VoteResponseDTO(deal.getDealId(), "핫딜 게시글 투표 성공");
    }

    // 단일 투표 점수 계산
    private double calculateSingleScore(User user, Deal deal, VoteType voteType, LocalDateTime now) {
        double userWeight = user.getUserWeight(); // 유저 가중치 DB에서 가져옴
        double timeDecay = getTimeDecay(deal.getCreatedAt(), now);
        double likeWeight = switch (voteType) {
            case TRUE -> 1.0;
            case FALSE -> -0.8;
            default -> 0.0;
        };
        double resistance = 2.0 + (deal.getHeat() / 100.0); // CurrentResistance = BaseResistance + CurrentHeat / ResistanceWeight
        return (userWeight * timeDecay * likeWeight / resistance);
    }

    // 생성 시간 감쇠율
    private double getTimeDecay(LocalDateTime createdAt, LocalDateTime now) {
        long hours = Duration.between(createdAt, now).toHours();
        if (hours < 0) return 1.0;
        if (hours < 1) return 1.0;
        if (hours < 6) return 0.8;
        if (hours < 24) return 0.6;
        if (hours < 72) return 0.3;
        return 0.1;
    }

    // 점수 범위 설정
    private double clampScore(double score) {
        return Math.max(-1000.0, Math.min(1000.0, score));
    }
}
