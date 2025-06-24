package com.cherrypick.backend.domain.comment.controller;

import com.cherrypick.backend.domain.comment.dto.request.CommentRequestDTOs;
import com.cherrypick.backend.domain.comment.dto.response.BestCommentResponseDTO;
import com.cherrypick.backend.domain.comment.dto.response.CommentListResponseDTO;
import com.cherrypick.backend.domain.comment.dto.response.CommentResponseDTOs;
import com.cherrypick.backend.domain.comment.enums.SortType;
import com.cherrypick.backend.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Comment", description = "댓글 CRUD")
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @Operation(
            summary = "댓글 생성 API V1",
            description = "댓글을 생성합니다. JWT 인증 필수입니다."
    )
    @PostMapping("/comment/{dealId}")
    public ResponseEntity<CommentResponseDTOs.Create> createComment(
            @PathVariable Long dealId,
            @RequestBody CommentRequestDTOs.Create request,
            @RequestParam(value = "version", defaultValue = "v1") String version) {

        CommentResponseDTOs.Create response = commentService.createComment(dealId, request);
        return ResponseEntity.ok(response);
    }

    // 댓글 전체 조회
    @Operation(
            summary = "댓글 전체 조회 API V1",
            description = "게시글 별 댓글을 전체 조회합니다. 삭제된 댓글도 포함되며, 기본 정렬은 최신순입니다."
    )
    @GetMapping("/comment/{dealId}")
    public ResponseEntity<List<CommentListResponseDTO>> getCommentList(
            @PathVariable Long dealId,
            @RequestParam(defaultValue = "LATEST") SortType sortType,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        return ResponseEntity.ok(commentService.getCommentList(dealId, sortType));
    }

    // 베스트 댓글 조회
    @Operation(
            summary = "베스트 댓글 조회 API V1",
            description = "베스트 댓글을 최대 2개 조회합니다. 삭제된 댓글은 제외하며, 없거나 겹치는 경우 최신순으로 상위 2개를 조회합니다."
    )
    @GetMapping("/best-comment/{dealId}")
    public ResponseEntity<List<BestCommentResponseDTO>> getBestComments(
            @PathVariable Long dealId,
            @RequestParam(value = "version", defaultValue = "v1") String version){
        return ResponseEntity.ok(commentService.getBestComments(dealId));
    }

    // 댓글 삭제
    @Operation(
            summary = "댓글 삭제 API V1",
            description = "댓글을 삭제합니다. JWT 인증 필수입니다."
    )
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponseDTOs.Delete> deleteComment(
            @PathVariable Long commentId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        return ResponseEntity.ok(commentService.deleteComment(commentId));
    }

    // 댓글 좋아요
    @Operation(
            summary = "댓글 좋아요 및 좋아요 취소 API V1",
            description = "댓글에 좋아요를 누르거나 취소합니다. boolean 타입으로 구분합니다. JWT 인증 필수입니다."
    )
    @PutMapping("/comment/like")
    public ResponseEntity<CommentResponseDTOs.Like> likeComment(
            @RequestBody CommentRequestDTOs.Like request,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        return ResponseEntity.ok(commentService.likeComment(request));
    }

    @PostMapping("/comment/dummy")
    public String createDummyComment(){
        commentService.dummyDataSetting();
        return "yesssssssss";
    }


}
