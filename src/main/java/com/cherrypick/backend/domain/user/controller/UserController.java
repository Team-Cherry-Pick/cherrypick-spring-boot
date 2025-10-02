package com.cherrypick.backend.domain.user.controller;

import com.cherrypick.backend.domain.user.dto.response.UserDetailResponseDTO;
import com.cherrypick.backend.domain.user.dto.request.UserRequestDTOs;
import com.cherrypick.backend.domain.user.dto.response.UserResponseDTOs;
import com.cherrypick.backend.domain.user.dto.request.UserUpdateRequestDTO;
import com.cherrypick.backend.domain.user.service.BadgeService;
import com.cherrypick.backend.domain.user.service.UserService;
import com.cherrypick.backend.global.util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/user")
@RequiredArgsConstructor @Slf4j @Tag(name="User", description = "유저 로직을 전담")
public class UserController {

    private final UserService userService;
    private final BadgeService badgeService;


    @Operation(
            summary = "닉네임 유효성 판별 API V1",
            description = "닉네임의 유효성을 판단하여 반환합니다. details에 상세 사유가 들어갑니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "닉네임 유효성을 판단하였습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/nickname-validation")
    public ResponseEntity<UserResponseDTOs.NicknameValidDTO> nicknameValidation(
            @Parameter(description = "유효성을 검증 받을 닉네임을 입력합니다.(파라미터)") String nickname,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(userService.nicknameValidation(nickname));
    }

    @Operation(
            summary = "유저 정보 수정 API V1",
            description = "이미지 수정에 대해서는 이미지 업로드 API를 경유 후 ID를 입력."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보를 수정하였습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("")
    public ResponseEntity<UserDetailResponseDTO> update(
            @Parameter(description = "유저 정보를 넘겨주십시오. (전부 넘겨주셔야함)") @RequestBody  UserUpdateRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        dto.validate(); // 요구 조건에 맞지 않으면 오류를 일으킴.
        return ResponseEntity.ok(userService.userUpdate(dto));
    }

    @Operation(
            summary = "유저 정보 상제 조회 API V1",
            description = "현재 인증 상태인 유저 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("")
    public ResponseEntity<UserDetailResponseDTO> getUserDetail(@RequestParam(value = "version", defaultValue = "v1") String version)
    {
        return ResponseEntity.ok(userService.getUserDetail());
    }


    @Operation(
            summary = "타겟 유저 정보 상제 조회 API V1",
            description = "해당 유저의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{user_id}")
    public ResponseEntity<UserDetailResponseDTO> getUserDetail(@PathVariable("user_id") Long user_id,
                                                               @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(userService.getUserDetail(user_id));
    }

    @Operation(
            summary = "유저 삭제 API V1",
            description = "해당 유저를 삭제합니다 (hard delete)"
    )
    @DeleteMapping("")
    public ResponseEntity<UserResponseDTOs.DeleteResponseDTO> deleteUser(@RequestParam(value = "version", defaultValue = "v1") String version, @RequestBody UserRequestDTOs.DeleteRequestDTO deleteRequestDTO)
    {

        return ResponseEntity.ok(userService.hardDelete(deleteRequestDTO));
    }

    @Operation(
            summary = "베타테스터 권한을 가진 사람의 목록 API V1",
            description = "베타테스터 권한을 가진 사람들의 숫자입니다.."
    )
    @GetMapping("/badge/beta-tester/count")
    public ResponseEntity<?> betaTesterCount(@RequestParam(value = "version", defaultValue = "v1") String version)
    {
        // 베타테스터 뱃지 ID는 2L
        var response = badgeService.getBadgeOwnerCount(2L);

        return ResponseEntity.ok(response);
    }



}
