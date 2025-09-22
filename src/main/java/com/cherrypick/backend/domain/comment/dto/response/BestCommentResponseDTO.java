package com.cherrypick.backend.domain.comment.dto.response;

import com.cherrypick.backend.domain.user.vo.UserVO;

public record BestCommentResponseDTO(
        Long commentId,
        UserVO userVO,
        int totalLikes,
        String content,
        boolean isLike
) {
}
