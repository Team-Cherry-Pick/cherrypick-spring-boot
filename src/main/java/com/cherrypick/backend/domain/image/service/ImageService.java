package com.cherrypick.backend.domain.image.service;

import com.cherrypick.backend.domain.image.dto.request.ImageUploadRequestDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageDeleteResponseDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.ImageErrorCode;
import com.cherrypick.backend.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service @Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;

    // 이미지 업로드
    public List<ImageUploadResponseDTO> uploadImages(ImageUploadRequestDTO dto) {
        MultipartFile[] images = dto.images();
        Integer[] indexes = dto.indexes();

        // 이미지 개수랑 인덱스 개수 안 맞으면 에러
        if (images.length != indexes.length) {
            throw new BaseException(ImageErrorCode.IMAGE_COUNT_MISMATCH);
        }

        List<ImageUploadResponseDTO> responses = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            int index = indexes[i];

            String url;
            try {
                url = s3Uploader.upload(image, "deal/123");
            } catch (BaseException e) {
                throw e; // 위임
            } catch (Exception e) {
                throw new BaseException(ImageErrorCode.IMAGE_UPLOAD_FAIL);
            }

            Image imageEntity = new Image();
            imageEntity.setImageUrl(url);
            imageEntity.setImageIndex(index);
            imageEntity.setImageType(null);
            imageEntity.setRefId(null);
            imageEntity.setTemp(true);

            imageRepository.save(imageEntity);

            responses.add(new ImageUploadResponseDTO(imageEntity.getImageId(), url, index));
        }

        return responses;
    }

    // 이미지 삭제
    @Transactional
    public ImageDeleteResponseDTO deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND));

        s3Uploader.delete(image.getImageUrl());
        imageRepository.delete(image);

        return new ImageDeleteResponseDTO("이미지 삭제 완료");
    }

    // 임시 이미지 삭제
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void cleanUpTempImages() {
        List<Image> tempImages = imageRepository.findAllByIsTempTrue();

        for (Image image : tempImages) {
            try {
                s3Uploader.delete(image.getImageUrl());
                imageRepository.delete(image);
            } catch (Exception ignored) {
            }
        }
    }

    // 이미지를 특정 대상에 연결하는 공통 메소드
    @Transactional
    public void attachImage(Long refId, List<Long> imageIds, ImageType imageType) {
        List<Image> images = imageRepository.findAllById(imageIds);

        for (Image image : images) {
            image.setRefId(refId);          // 게시글 ID나 유저 ID
            image.setImageType(imageType);  // DEAL / USER
            image.setTemp(false);           // false로 update
        }
    }

    // 이미지 순서 수정 대비
    @Transactional
    public void attachAndIndexImages(Long refId, List<ImageUrl> imageUrls, ImageType imageType) {
        for (ImageUrl imageUrl : imageUrls) {
            Image image = imageRepository.findById(imageUrl.imageId())
                    .orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND));

            image.setRefId(refId);
            image.setImageType(imageType);
            image.setTemp(false);
            image.setImageIndex(imageUrl.indexes());
        }
    }

    @Transactional
    public Image getImageByRefId(Long refId, ImageType imageType) {

        var image = imageRepository.findByRefId(refId, imageType);
        return image.orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND));
    }

    // 이미지 삭제
    @Transactional
    public ImageDeleteResponseDTO deleteImageByUserId(Long userId) {

        var image = imageRepository.findByRefId(userId, ImageType.USER);
        if(image.isEmpty()) return new ImageDeleteResponseDTO("해당 유저는 프로필 사진이 없습니다.");
        return deleteImage(image.map(Image::getRefId).orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND)));
    }

}
