package com.cherrypick.backend.domain.comment.repository;

import com.cherrypick.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 dealId에 대한 댓글 수 카운트
    long countByDealId_DealIdAndIsDeleteFalse(Long dealId);

}
