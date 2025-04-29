package com.cherrypick.backend.domain.comment.dto.response;

import com.cherrypick.backend.domain.user.vo.User;

public record BestCommentResponseDTO(
        Long commentId,
        User user,
        int totalLikes,
        String content
) {
}
