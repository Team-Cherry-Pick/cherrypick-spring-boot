package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements BaseErrorCode {



    // ✅ 400 BAD REQUEST
    INVALID_COMMENT_REQUEST(HttpStatus.BAD_REQUEST, "댓글 요청이 올바르지 않습니다."),
    MISSING_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "댓글 내용이 누락되었습니다."),
    INVALID_PARENT_COMMENT(HttpStatus.BAD_REQUEST, "부모 댓글 정보가 유효하지 않습니다."),
    CANNOT_REPLY_TO_REPLY(HttpStatus.BAD_REQUEST, "대댓글에는 답글을 작성할 수 없습니다."),

    // ✅ 404 NOT FOUND
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    PARENT_COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 부모 댓글입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글 작성자를 찾을 수 없습니다."),
    NO_COMMENTS_FOUND(HttpStatus.NOT_FOUND, "해당 딜에 등록된 댓글이 없습니다.");

    private final HttpStatus status;
    private final String message;

    CommentErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
