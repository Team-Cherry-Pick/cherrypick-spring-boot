package com.cherrypick.backend.domain.deal.controller;

import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/deal")
public class DealController {

    private final DealService dealService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<DealResponseDTOs.Create> createDeal(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestBody DealCreateRequestDTO dealCreateRequestDTO) {

        DealResponseDTOs.Create response = dealService.createDeal(dealCreateRequestDTO);
        return ResponseEntity.status(201).body(response);
    }

    // 게시글 상세 조회
    @GetMapping("/{dealId}")
    public ResponseEntity<DealDetailResponseDTO> getDealDetail(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.getDealDetail(dealId));
    }

    // 게시물 수정
    @PatchMapping
    public ResponseEntity<DealResponseDTOs.Update> updateDeal(
            @RequestBody DealUpdateRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.updateDeal(dto));
    }

    // 게시글 삭제
    @DeleteMapping("/{dealId}")
    public ResponseEntity<DealResponseDTOs.Delete> deleteDeal(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.deleteDeal(dealId));
    }
}
