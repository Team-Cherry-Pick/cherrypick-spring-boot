package com.cherrypick.backend.domain.vote.repository;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.vote.entity.Vote;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 특정 dealId에 대해 추천(좋아요)의 개수
    int countByDealIdAndVoteType(Deal deal, VoteType voteType);

    // 중복 투표 검사
    Optional<Vote> findByUserIdAndDealId(User userId, Deal dealId);
}
