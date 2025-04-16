package com.cherrypick.backend.domain.vote.controller;

import com.cherrypick.backend.domain.vote.dto.request.VoteRequestDTO;
import com.cherrypick.backend.domain.vote.dto.response.VoteResponseDTO;
import com.cherrypick.backend.domain.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deal")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PutMapping("/{dealId}/vote")
    public ResponseEntity<VoteResponseDTO> vote(
            @PathVariable Long dealId,
            @RequestBody VoteRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version
    ) {
        VoteResponseDTO response = voteService.vote(dealId, dto);
        return ResponseEntity.ok(response);
    }
}
