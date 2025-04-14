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
    public DealResponseDTOs.Create createDeal(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestBody DealCreateRequestDTO dealCreateRequestDTO) {
        return dealService.createDeal(dealCreateRequestDTO);
    }

    // 게시글 상세 조회
    @GetMapping("/{dealId}")
    public ResponseEntity<DealDetailResponseDTO> getDealDetail(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        DealDetailResponseDTO response = dealService.getDealDetail(dealId);
        return ResponseEntity.ok(response);
    }

    // 게시물 수정
    @PatchMapping()
    public ResponseEntity<DealResponseDTOs.Update> updateDeal(
            @RequestBody DealUpdateRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        return ResponseEntity.ok(dealService.updateDeal(dto));
    }

}
