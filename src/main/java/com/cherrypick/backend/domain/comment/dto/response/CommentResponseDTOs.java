package com.cherrypick.backend.domain.comment.dto.response;

public class CommentResponseDTOs {

    public record Create(Long commentID, String message){}
    public record Delete(Long commentID, String message){}
    public record Like(Long commentID, String message){}
}
