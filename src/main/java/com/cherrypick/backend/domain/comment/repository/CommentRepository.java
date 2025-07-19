package com.cherrypick.backend.domain.comment.repository;

import com.cherrypick.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 dealId에 대한 댓글 수 카운트
    long countByDealId_DealIdAndIsDeleteFalse(Long dealId);

    // 특정 dealId에 대한 모든 댓글들 조회
    @Query("SELECT c FROM Comment c WHERE c.dealId.dealId = :dealId")
    List<Comment> findAllByDealId(Long dealId);

    // 특정 dealId에 대한 지워지지 않은 댓글들 조회 (베스트 댓글 조회용)
    @Query("""
    SELECT c
    FROM Comment c
    WHERE c.dealId.dealId = :dealId
      AND c.isDelete = false
    """)
    List<Comment> findAllByDealIdAndIsDeleteFalse(@Param("dealId") Long dealId);

    // 부모 댓글 최신순 (삭제 여부 무시)
    @Query("""
    SELECT c FROM Comment c
    WHERE c.dealId.dealId = :dealId AND c.parentId IS NULL
    ORDER BY c.createdAt DESC
    """)
    List<Comment> findParentCommentsLatest(Long dealId);

    // 부모 댓글 좋아요순 (삭제 여부 무시)
    @Query("""
    SELECT c FROM Comment c
    WHERE c.dealId.dealId = :dealId AND c.parentId IS NULL
    ORDER BY (SELECT COUNT(l) FROM CommentLike l WHERE l.commentId = c) DESC, c.createdAt DESC
    """)
    List<Comment> findParentCommentsByLikes(Long dealId);

    // 대댓글 (삭제 여부 무시, 작성된 순서대로)
    @Query("""
    SELECT c FROM Comment c
    WHERE c.parentId = :parentId
    ORDER BY c.createdAt ASC
    """)
    List<Comment> findReplies(Long parentId);

    @Query("SELECT c.dealId.dealId, COUNT(c) FROM Comment c WHERE c.dealId.dealId IN :dealIds AND c.isDelete = false GROUP BY c.dealId.dealId")
    List<Object[]> countCommentsGroupedByDealId(@Param("dealIds") List<Long> dealIds);

    default Map<Long, Long> countByDealIdsGrouped(List<Long> dealIds) {
        List<Object[]> result = countCommentsGroupedByDealId(dealIds);
        return result.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));
    }


}
