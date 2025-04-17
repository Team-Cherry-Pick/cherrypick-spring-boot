package com.cherrypick.backend.domain.image.controller;

import com.cherrypick.backend.domain.image.dto.request.ImageUploadRequestDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageDeleteResponseDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.cherrypick.backend.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<List<ImageUploadResponseDTO>> uploadImages(
            @ModelAttribute ImageUploadRequestDTO dto,
            @RequestParam(value = "version", defaultValue = "v1") String version
    ) {
        List<ImageUploadResponseDTO> response = imageService.uploadImages(dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ImageDeleteResponseDTO> deleteImage(
            @PathVariable Long imageId,
            @RequestParam(value = "version", defaultValue = "v1") String version) {
        ImageDeleteResponseDTO response = imageService.deleteImage(imageId);
        return ResponseEntity.ok(response);
    }
}
