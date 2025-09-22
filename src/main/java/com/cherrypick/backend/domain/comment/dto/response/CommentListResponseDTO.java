package com.cherrypick.backend.domain.comment.dto.response;

import com.cherrypick.backend.domain.user.vo.UserVO;

import java.time.LocalDateTime;
import java.util.List;

public record CommentListResponseDTO(
        Long commentId,
        Long parentId,
        UserVO userVO,
        String content,
        int totalLikes,
        int totalReplys,
        LocalDateTime createdAt,
        boolean isDelete,
        List<CommentListResponseDTO> replies,
        boolean isLike
) { }
