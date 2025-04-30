package com.cherrypick.backend.domain.vote.controller;

import com.cherrypick.backend.domain.vote.dto.request.VoteRequestDTO;
import com.cherrypick.backend.domain.vote.dto.response.VoteResponseDTO;
import com.cherrypick.backend.domain.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Vote", description = "게시글 추천과 비추천")
@RequestMapping("/api/deal")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @Operation(
            summary = "게시글 투표 API V1",
            description = "게시글을 투표합니다. TRUE, FALSE, NONE 형태가 존재하며 FALSE일 경우는 비추천 사유가 필수입니다. JWT 인증 필수입니다."
    )
    @PutMapping("/{dealId}/vote")
    public ResponseEntity<VoteResponseDTO> vote(
            @PathVariable Long dealId,
            @RequestBody VoteRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version
    ) {
        VoteResponseDTO response = voteService.createVote(dealId, dto);
        return ResponseEntity.ok(response);
    }
}
