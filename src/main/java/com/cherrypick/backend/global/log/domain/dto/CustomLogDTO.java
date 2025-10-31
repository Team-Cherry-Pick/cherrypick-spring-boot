package com.cherrypick.backend.global.log.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.HashMap;

@Schema(description = "웹 커스텀 로그 요청 DTO")
public record CustomLogDTO(
        @Schema(description = "로그 타입", example = "BUTTON_CLICK", required = true)
        String logType,

        @Schema(description = "로그 데이터 (자유 형식의 키-값)", example = "{\"page\": \"/deals\", \"buttonName\": \"like\"}", required = true)
        HashMap<String, Object> logMap
) {
}
