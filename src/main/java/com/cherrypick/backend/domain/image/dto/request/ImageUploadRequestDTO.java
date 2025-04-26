package com.cherrypick.backend.domain.image.dto.request;

import com.cherrypick.backend.domain.image.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

public record ImageUploadRequestDTO(
        MultipartFile[] images,
        Integer[] indexes
) {}


