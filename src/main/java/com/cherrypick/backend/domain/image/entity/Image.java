package com.cherrypick.backend.domain.image.entity;

import com.cherrypick.backend.domain.image.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @RequiredArgsConstructor
@Setter @AllArgsConstructor @Builder
@EntityListeners(AuditingEntityListener.class)
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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

}
