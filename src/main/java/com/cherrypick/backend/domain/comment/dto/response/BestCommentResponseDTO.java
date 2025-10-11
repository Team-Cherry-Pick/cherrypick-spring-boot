package com.cherrypick.backend.domain.comment.dto.response;

import com.cherrypick.backend.domain.user.vo.AuthorDTO;

public record BestCommentResponseDTO(
        Long commentId,
        AuthorDTO user,
        int totalLikes,
        String content,
        boolean isLike
) {
}
