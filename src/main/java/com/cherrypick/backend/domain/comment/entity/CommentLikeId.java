package com.cherrypick.backend.domain.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CommentLikeId implements Serializable {

    private Long userId;
    private Long commentId;

    // 복합키 로직에서는 @AllArgsConstructor 보다 직접 생성자에서 처리하는 게 더 유리함
    public CommentLikeId(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
