package com.cherrypick.backend.domain.comment.service;

import com.cherrypick.backend.domain.comment.dto.request.CommentRequestDTOs;
import com.cherrypick.backend.domain.comment.dto.response.CommentResponseDTOs;
import com.cherrypick.backend.domain.comment.entity.Comment;
import com.cherrypick.backend.domain.comment.entity.CommentLike;
import com.cherrypick.backend.domain.comment.entity.CommentLikeId;
import com.cherrypick.backend.domain.comment.repository.CommentLikeRepository;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.user.dto.UserDetailDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.CommentErrorCode;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDTOs.Create createComment(Long dealId, CommentRequestDTOs.Create request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 부모 댓글 없는 경우 (parentId가 왔을 때)
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BaseException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 대댓글에 대댓글을 단 경우
            if (parentComment.getParentId() != null) {
                throw new BaseException(CommentErrorCode.CANNOT_REPLY_TO_REPLY);
            }
        }

        // 댓글 내용 없는 경우
        if (request.content() == null || request.content().isBlank()) {
            throw new BaseException(CommentErrorCode.MISSING_COMMENT_CONTENT);
        }

        Comment comment = new Comment();
        comment.setDealId(deal);
        comment.setParentId(request.parentId());
        comment.setUserId(user);
        comment.setContent(request.content());
        comment.setDelete(false);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        return new CommentResponseDTOs.Create(comment.getCommentId(), "댓글 작성 성공");
    }

    // 댓글 삭제 (Soft Delete)
    @Transactional
    public CommentResponseDTOs.Delete deleteComment(Long commentId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 검증
        if (!comment.getUserId().getUserId().equals(user.getUserId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        comment.setDelete(true);

        return new CommentResponseDTOs.Delete(comment.getCommentId(), "댓글 삭제 성공");
    }

    // 댓글 좋아요
    @Transactional
    public CommentResponseDTOs.Like likeComment(CommentRequestDTOs.Like request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UserDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Comment comment = commentRepository.findById(request.commentID())
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_NOT_FOUND));

        CommentLikeId likeId = new CommentLikeId(user.getUserId(), comment.getCommentId());

        boolean exists = commentLikeRepository.existsById(likeId);

        String message;

        if (request.isLike()) {
            // 좋아요 추가
            if (!exists) {
                CommentLike like = new CommentLike(comment, user);
                commentLikeRepository.save(like);
            }
            message = "댓글 좋아요 성공";
        } else {
            // 좋아요 취소
            if (exists) {
                commentLikeRepository.deleteById(likeId);
            }
            message = "댓글 좋아요 취소 성공";
        }

        return new CommentResponseDTOs.Like(comment.getCommentId(), message);
    }
}
