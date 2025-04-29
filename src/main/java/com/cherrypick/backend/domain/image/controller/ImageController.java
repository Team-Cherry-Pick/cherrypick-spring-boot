package com.cherrypick.backend.domain.image.controller;

import com.cherrypick.backend.domain.image.dto.request.ImageUploadRequestDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageDeleteResponseDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.cherrypick.backend.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Image", description = "이미지 생성 및 삭제")
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    // 이미지 업로드
    @Operation(
            summary = "이미지 업로드 API V1",
            description = "이미지를 업로드하면 S3 URL을 반환합니다. 인덱스와 이미지 개수가 매칭돼야 하며 인덱스는 0부터 시작합니다."
    )
    @PostMapping
    public ResponseEntity<List<ImageUploadResponseDTO>> uploadImages(
            @ModelAttribute ImageUploadRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version
    ) {
        List<ImageUploadResponseDTO> response = imageService.createImages(dto);
        return ResponseEntity.ok(response);
    }

    // 이미지 삭제
    @Operation(
            summary = "이미지 삭제 API V1",
            description = "이미지를 RDS 및 S3에서 삭제합니다. 매일 새벽 3시 자동 실행되어 임시 이미지들이 삭제됩니다."
    )
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ImageDeleteResponseDTO> deleteImage(
            @PathVariable Long imageId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        ImageDeleteResponseDTO response = imageService.deleteImage(imageId);
        return ResponseEntity.ok(response);
    }
}
