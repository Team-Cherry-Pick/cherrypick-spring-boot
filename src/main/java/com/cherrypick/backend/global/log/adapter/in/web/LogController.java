package com.cherrypick.backend.global.log.adapter.in.web;

import com.cherrypick.backend.global.log.domain.LogService;
import com.cherrypick.backend.global.log.domain.dto.CustomLogDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
@Tag(name = "Log", description = "클라이언트 로그 수집 API")
public class LogController
{
    private final LogService logService;

    @Operation(
            summary = "웹 커스텀 로그 수집 API",
            description = "프론트엔드에서 발생한 사용자 행동, 이벤트, 오류 등을 서버에 기록합니다. " +
                    "logType으로 로그 종류를 구분하고, logMap에 자유로운 형식의 데이터를 담아 전송합니다. " +
                    "민감정보(비밀번호, 토큰 등)는 절대 포함하지 마세요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그가 성공적으로 기록되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("")
    public ResponseEntity<String> webCustomLog(@RequestBody CustomLogDTO request)
    {
        logService.webCustomLog(request.logType(), request.logMap());
        return ResponseEntity.ok("success");
    }

}
