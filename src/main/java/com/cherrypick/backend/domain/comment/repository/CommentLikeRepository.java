package com.cherrypick.backend.domain.comment.repository;

import com.cherrypick.backend.domain.comment.entity.Comment;
import com.cherrypick.backend.domain.comment.entity.CommentLike;
import com.cherrypick.backend.domain.comment.entity.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {

    int countByCommentId(Comment comment);
}
