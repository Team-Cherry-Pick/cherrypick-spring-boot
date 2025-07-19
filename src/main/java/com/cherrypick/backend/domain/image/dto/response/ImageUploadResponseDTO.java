package com.cherrypick.backend.domain.image.dto.response;

public record ImageUploadResponseDTO (

        Long imageId,
        String imageUrl,
        int indexes
){
}
