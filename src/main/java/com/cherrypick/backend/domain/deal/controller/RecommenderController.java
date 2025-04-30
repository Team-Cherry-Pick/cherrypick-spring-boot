package com.cherrypick.backend.domain.deal.controller;

import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.service.RecommenderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller@RequiredArgsConstructor
@RequestMapping("/api")
public class RecommenderController {
    private final RecommenderService recommenderService;


    @Operation(
            summary = "유저 행동 로그 생성",
            description = "유저 행동 로그를 생성합니다."
    )
    @PostMapping("/user/{user_id}/logs")
    @ResponseBody
    public ResponseEntity<?> createLog(@RequestBody DealRequestDTOs.UserBehaviorDTO logDto)
    {

        return ResponseEntity.ok(recommenderService.addUserBehaviorLog(logDto));
    }

    @Operation(
            summary = "유저 행동 로그 조회",
            description = "유저 행동 로그를 조회합니다."
    )
    @GetMapping("/user/{user_id}/logs")
    @ResponseBody
    public ResponseEntity<?> viewLog(@PathVariable("user_id") Long userId)
    {

        return ResponseEntity.ok(recommenderService.getLogByUserId(userId));
    }

    @Operation(
            summary = "유저 선호 태그 조회",
            description = "유저가 선호하는 태그를 조회합니다."
    )
    @GetMapping("/user/{user_id}/tags")
    public ResponseEntity<?> getUserHashWeight(@PathVariable("user_id") Long userId)
    {

        return ResponseEntity.ok(recommenderService.getUserInterestWeight(userId));
    }

    @Operation(
            summary = "태그 유사도 조회",
            description = "각 해시태그 간 유사도를 조회합니다."
    )
    @GetMapping("/tags")
    public ResponseEntity<?> getTagsSimilarity(String name)
    {
        return ResponseEntity.ok(recommenderService.getTagsSimilarity(name));
    }

    @Operation(
            summary = "추천 딜 조회",
            description = "추천 시스템 기반의 딜 추천을 조회합니다. 딜 검색과 같은 반환 값을 갖습니다 ."
    )
    @GetMapping("/user/{user_id}/recommand-item")
    public ResponseEntity<?> getTagsSimilarity(@PathVariable("user_id") Long userId)
    {
        return ResponseEntity.ok(recommenderService.getInterestBoard(userId));
    }

}
