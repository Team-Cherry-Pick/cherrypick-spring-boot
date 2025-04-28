package com.cherrypick.backend.domain.user.controller;

import com.cherrypick.backend.domain.user.dto.UserResponseDTOs;
import com.cherrypick.backend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor @Slf4j @Tag(name="유저 컨트롤러", description = "유저 로직을 전담")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "닉네임의 유효성을 판단하여 반환합니다.",
            description = "details에 상세 사유가 들어갑니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 유효성을 판단하였습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/nickname-validation")
    public ResponseEntity<UserResponseDTOs.NicknameValidDTO> nicknameValidation(@Parameter(description = "유효성을 검증 받을 닉네임을 입력합니다.(파라미터)") String nickname) {

        return ResponseEntity.ok(userService.nicknameValidation(nickname));
    }




}
