package com.cherrypick.backend.domain.deal.controller;

import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.deal.service.RecommenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller@RequiredArgsConstructor
public class RecommenderController {
    private final RecommenderService recommenderService;

    @GetMapping("/dashboard")
    public String dashboard()
    {
        return "dashboard";
    }


    @PostMapping("/user/{user_id}/logs")
    @ResponseBody
    public ResponseEntity<?> createLog(@RequestBody DealRequestDTOs.UserBehaviorDTO logDto)
    {

        return ResponseEntity.ok(recommenderService.addUserBehaviorLog(logDto));
    }

    @GetMapping("/user/{user_id}/logs")
    @ResponseBody
    public ResponseEntity<?> viewLog(@PathVariable("user_id") Long userId)
    {

        return ResponseEntity.ok(recommenderService.getLogByUserId(userId));
    }
/*
    @GetMapping("/user/{user_id}/tags")
    @ResponseBody
    public ResponseEntity<?> getUserHashWeight(@PathVariable("user_id") int userId)
    {

        return ResponseEntity.ok(dashBoardService.getUserInterestWeight(userId));
    }

    @GetMapping("/tags")
    @ResponseBody
    public ResponseEntity<?> getTagsSimilarity(String name)
    {
        return ResponseEntity.ok(dashBoardService.getTagsSimilarity(name));
    }

    @GetMapping("/user/{user_id}/recommand-item")
    @ResponseBody
    public ResponseEntity<?> getTagsSimilarity(@PathVariable("user_id") Integer userId)
    {
        return ResponseEntity.ok(dashBoardService.getInterestBoard(userId));
    }

     */

}
