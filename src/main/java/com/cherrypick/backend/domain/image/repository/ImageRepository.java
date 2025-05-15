package com.cherrypick.backend.domain.image.repository;

import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // isTemp = true인 이미지 전체 조회
    List<Image> findAllByIsTempTrue();

    // 썸네일
    Optional<Image> findTopByRefIdAndImageTypeOrderByImageIndexAsc(Long refId, ImageType imageType);

    // 게시글 이미지 전체조회
    List<Image> findByRefIdAndImageTypeOrderByImageIndexAsc(Long refId, ImageType imageType);

    // 이미지 조회
    @Query(value = "SELECT * FROM image WHERE ref_id=:refId and image_type='USER'", nativeQuery = true)
    Optional<Image> findByUserId(@Param("refId")Long refId);

    @Query("""
    SELECT i FROM Image i
    WHERE i.refId IN :dealIds AND i.imageType = :imageType
    AND i.imageIndex = (
        SELECT MIN(i2.imageIndex) FROM Image i2 
        WHERE i2.refId = i.refId AND i2.imageType = :imageType
    )
""")
    List<Image> findTopImagesByDealIds(@Param("dealIds") List<Long> dealIds, @Param("imageType") ImageType imageType);

}
