package com.cherrypick.backend.domain.image.entity;

import com.cherrypick.backend.domain.image.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @RequiredArgsConstructor
@Setter @AllArgsConstructor @Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    private Long refId;

    private int imageIndex;

    private boolean isTemp;
}
