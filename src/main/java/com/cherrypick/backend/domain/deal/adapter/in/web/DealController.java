package com.cherrypick.backend.domain.deal.adapter.in.web;

import com.cherrypick.backend.domain.deal.application.dto.request.DealCreateRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.request.DealSearchRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.request.DealUpdateRequestDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.DealResponseDTOs;
import com.cherrypick.backend.domain.deal.application.dto.response.DealSearchPageResponseDTO;
import com.cherrypick.backend.domain.deal.application.service.CreateDealUseCase;
import com.cherrypick.backend.domain.deal.application.service.DealLogService;
import com.cherrypick.backend.domain.deal.application.service.DealService;
import com.cherrypick.backend.domain.deal.domain.service.DealCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Deal", description = "핫딜 게시글 CRUD")
@RestController
@RequestMapping("/api") @Slf4j
public class DealController {

    private final DealService dealService;
    private final DealLogService dealLogService;
    private final CreateDealUseCase createDealUseCase;

    // 게시글 생성
    @Operation(
            summary = "핫딜 게시글 생성 API V1",
            description = "핫딜 게시글을 생성합니다. JWT 인증 필수입니다."
    )
    @PostMapping("/deal")
    public ResponseEntity<DealResponseDTOs.Create> createDeal(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestBody DealCreateRequestDTO dealCreateRequestDTO) {

        DealResponseDTOs.Create response = createDealUseCase.createDeal(dealCreateRequestDTO);

        return ResponseEntity.ok(response);
    }

    // 게시글 전체 조회 (검색)
    @Operation(
            summary = "핫딜 게시글 전제 조회 API V1",
            description = "핫딜 게시글을 전체 조회합니다. 필터를 보낼 경우 부분 조회(검색) 기능을 합니다."
    )
    @PostMapping("/search/deal")
    public ResponseEntity<DealSearchPageResponseDTO> searchDeals(
            @RequestParam(value = "version", defaultValue = "v1") String version,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "40") int size,
            @RequestBody(required = false) DealSearchRequestDTO request) {

        if (request == null) {
            request = new DealSearchRequestDTO();
        }

        DealSearchPageResponseDTO response = dealService.searchDeals(request, page, size);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세 조회
    @Operation(
            summary = "핫딜 게시글 상세 조회 API V1",
            description = "핫딜 게시글을 상세 조회합니다."
    )
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<DealDetailResponseDTO> getDealDetail(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version,
            HttpServletRequest request) {

        String deviceId = request.getHeader("Device-Id");
        return ResponseEntity.ok(dealService.getDealDetail(dealId, deviceId));
    }

    // 게시물 수정
    @Operation(
            summary = "핫딜 게시글 수정 API V1",
            description = "핫딜 게시글을 수정합니다. 수정 할 부분만 요청하면 됩니다. JWT 인증 필수입니다."
    )
    @PatchMapping("/deal")
    public ResponseEntity<DealResponseDTOs.Update> updateDeal(
            @RequestBody DealUpdateRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.updateDeal(dto));
    }

    // 게시글 삭제
    @Operation(
            summary = "핫딜 게시글 삭제 API V1",
            description = "핫딜 게시글을 삭제합니다. SoftDelete 방식이며 JWT 인증 필수입니다."
    )
    @DeleteMapping("/deal/{dealId}")
    public ResponseEntity<DealResponseDTOs.Delete> deleteDeal(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        return ResponseEntity.ok(dealService.deleteDeal(dealId));
    }

    // 구매로그
    @Operation(
            summary = "핫딜 게시글 구매 버튼 로그 추가 API V1",
            description = "구매버튼을 눌렀다는 이력의 로그를 찍습니다."
    )
    @GetMapping("/deal/purchase-log")
    public ResponseEntity<String> purchaseLog(
            @RequestParam(value = "dealId", defaultValue = "1") Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version,
            HttpServletRequest request) {

        String deviceId = request.getHeader("Device-Id");

        return ResponseEntity.ok(dealLogService.putPurchaseClickLog(dealId, deviceId));
    }
    // 게시글 삭제
    @Operation(
            summary = "핫딜 게시글 공유 버튼 로그 추가 API V1",
            description = "공유버튼을 눌렀다는 이력의 로그를 찍습니다."
    )
    @GetMapping("/deal/share-log")
    public ResponseEntity<String> shareLog(
            @RequestParam(value = "dealId", defaultValue = "1") Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version,
            HttpServletRequest request) {

        String deviceId = request.getHeader("Device-Id");

        return ResponseEntity.ok(dealLogService.putShareClickLog(dealId, deviceId));
    }
}
