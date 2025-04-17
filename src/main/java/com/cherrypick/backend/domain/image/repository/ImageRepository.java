package com.cherrypick.backend.domain.image.repository;

import com.cherrypick.backend.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // isTemp = true인 이미지 전체 조회
    List<Image> findAllByIsTempTrue();
}
