package com.cherrypick.backend.domain.deal.controller;

import com.cherrypick.backend.domain.deal.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.dto.response.DealSearchResponseDTO;
import com.cherrypick.backend.domain.deal.service.DealCrawlService;
import com.cherrypick.backend.domain.deal.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class DealController {

    private final DealService dealService;
    private final DealCrawlService dealCrawlService;

    // 게시글 생성
    @PostMapping("/deal")
    public ResponseEntity<DealResponseDTOs.Create> createDeal(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestBody DealCreateRequestDTO dealCreateRequestDTO) {

        DealResponseDTOs.Create response = dealService.createDeal(dealCreateRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 게시글 전체 조회 (검색)
    @PostMapping("/search/deal")
    public ResponseEntity<List<DealSearchResponseDTO>> searchDeals(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestBody(required = false) DealSearchRequestDTO request) {

        if (request == null) {
            request = new DealSearchRequestDTO(); // 비었을 시 전체조회
        }

        List<DealSearchResponseDTO> response = dealService.searchDeals(request);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세 조회
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<DealDetailResponseDTO> getDealDetail(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.getDealDetail(dealId));
    }

    // 게시물 수정
    @PatchMapping("/deal")
    public ResponseEntity<DealResponseDTOs.Update> updateDeal(
            @RequestBody DealUpdateRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.updateDeal(dto));
    }

    // 게시글 삭제
    @DeleteMapping("/deal/{dealId}")
    public ResponseEntity<DealResponseDTOs.Delete> deleteDeal(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.deleteDeal(dealId));
    }

    // 크롤링 API
    @GetMapping("/crawl-board")
    public String crawlBoard(String count) {
        try {
            dealCrawlService.crawlAndSaveBoard(Integer.parseInt(count));
            return "게시글 크롤링 및 저장 완료";
        } catch (Exception e) {
            return "오류 발생: " + e.getMessage();
        }
    }
}
