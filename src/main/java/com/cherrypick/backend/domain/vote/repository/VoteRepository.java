package com.cherrypick.backend.domain.vote.repository;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.vote.entity.Vote;
import com.cherrypick.backend.domain.vote.enums.voteType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 특정 dealId에 대해 추천(좋아요)의 개수
    int countByDealIdAndVoteType(Deal deal, voteType voteType);
}
