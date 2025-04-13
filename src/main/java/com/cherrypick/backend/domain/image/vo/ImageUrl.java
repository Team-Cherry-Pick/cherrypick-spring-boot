package com.cherrypick.backend.domain.image.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public class ImageUrl {

    private Long imageId;
    private String url;
    private int index;
}
