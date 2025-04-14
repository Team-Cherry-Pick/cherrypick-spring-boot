package com.cherrypick.backend.domain.image.entity;

import com.cherrypick.backend.domain.image.enums.ImageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private String imageUrl;

    private ImageType imageType;

    private Long refId;

    private int imageIndex;

    private boolean isTemp;
}
