package com.cherrypick.backend.global.exception.enums;

import com.cherrypick.backend.global.exception.BaseErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ImageErrorCode implements BaseErrorCode {

    IMAGE_COUNT_MISMATCH(HttpStatus.BAD_REQUEST, "이미지와 인덱스의 개수가 일치하지 않습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드 중 오류가 발생했습니다."),
    IMAGE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "이미지 용량이 초과 됐습니다."),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다."),
    IMAGE_READ_FAILED(HttpStatus.BAD_REQUEST, "이미지를 읽는 데 실패했습니다."),
    IMAGE_COMPRESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 압축에 실패했습니다."),
    IMAGE_S3_PUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3에 업로드하는 데 실패했습니다."),
    IMAGE_S3_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 URL 생성에 실패했습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    IMAGE_S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3에서 이미지 삭제에 실패했습니다."),
    IMAGE_URL_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 이미지 URL입니다.");

    private final HttpStatus status;
    private final String message;

    ImageErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}