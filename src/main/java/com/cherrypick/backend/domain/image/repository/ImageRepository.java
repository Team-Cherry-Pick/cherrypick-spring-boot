package com.cherrypick.backend.domain.image.repository;

import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // isTemp = true인 이미지 전체 조회
    List<Image> findAllByIsTempTrue();

    // 썸네일
    Optional<Image> findTopByRefIdAndImageTypeOrderByImageIndexAsc(Long refId, ImageType imageType);

    // 게시글 이미지 전체조회
    List<Image> findByRefIdAndImageTypeOrderByImageIndexAsc(Long refId, ImageType imageType);
}
