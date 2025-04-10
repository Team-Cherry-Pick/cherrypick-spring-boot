package com.cherrypick.backend.global.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public BaseException(BaseErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
