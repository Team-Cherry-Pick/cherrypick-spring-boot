package com.cherrypick.backend.domain.comment.dto.request;

public class CommentRequestDTOs {

    public record Create(Long parentId, String content){}
    public record Like(Long commentId, boolean isLike){}
}
