package com.cherrypick.backend.domain.vote.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.vote.entity.Vote;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 특정 dealId에 대해 추천(좋아요)의 개수
    int countByDealIdAndVoteType(Deal deal, VoteType voteType);

    // 중복 투표 검사
    Optional<Vote> findByUserIdAndDealId(User userId, Deal dealId);

    @Query("SELECT v.dealId.dealId, COUNT(v) FROM Vote v WHERE v.dealId.dealId IN :dealIds AND v.voteType = :voteType GROUP BY v.dealId.dealId")
    List<Object[]> countVotesGroupedByDealId(@Param("dealIds") List<Long> dealIds, @Param("voteType") VoteType voteType);

    default Map<Long, Long> countByDealIdsAndVoteTypeGrouped(List<Long> dealIds, VoteType voteType) {
        List<Object[]> result = countVotesGroupedByDealId(dealIds, voteType);
        return result.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }

}
