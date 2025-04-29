package com.cherrypick.backend.domain.comment.controller;

import com.cherrypick.backend.domain.comment.dto.request.CommentRequestDTOs;
import com.cherrypick.backend.domain.comment.dto.response.CommentResponseDTOs;
import com.cherrypick.backend.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Comment", description = "댓글 CRUD")
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @Operation(
            summary = "댓글 생성 API V1",
            description = "댓글을 생성합니다. JWT 인증 필수입니다."
    )
    @PostMapping("/{dealId}")
    public ResponseEntity<CommentResponseDTOs.Create> createComment(
            @PathVariable Long dealId,
            @RequestBody CommentRequestDTOs.Create request,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        CommentResponseDTOs.Create response = commentService.createComment(dealId, request);
        return ResponseEntity.ok(response);
    }
}
