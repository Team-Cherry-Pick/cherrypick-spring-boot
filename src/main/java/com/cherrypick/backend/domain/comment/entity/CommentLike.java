package com.cherrypick.backend.domain.comment.entity;

import com.cherrypick.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CommentLike {

    @EmbeddedId
    private CommentLikeId commentLikeId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id")
    private Comment commentId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User userId;

    // 복합키 로직에서는 @AllArgsConstructor 보다 직접 생성자에서 처리하는 게 더 유리함
    public CommentLike(Comment comment, User user) {
        this.commentId = comment;
        this.userId = user;
        this.commentLikeId = new CommentLikeId(user.getUserId(), comment.getCommentId());
    }

}
