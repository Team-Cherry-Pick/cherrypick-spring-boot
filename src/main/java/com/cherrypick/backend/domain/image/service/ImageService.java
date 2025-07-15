package com.cherrypick.backend.domain.image.service;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.image.dto.request.ImageUploadRequestDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageDeleteResponseDTO;
import com.cherrypick.backend.domain.image.dto.response.ImageUploadResponseDTO;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import com.cherrypick.backend.global.exception.enums.ImageErrorCode;
import com.cherrypick.backend.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final DealRepository dealRepository;

    // 이미지 업로드
    public List<ImageUploadResponseDTO> createImages(ImageUploadRequestDTO dto) {
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
        // 로그인 사용자 가져오기
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AuthenticatedUser userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND));

        // 권한 체크
        boolean isAdmin = userDetails.role() == Role.ADMIN;
        boolean isOwner = false;

        if (image.getImageType() == ImageType.DEAL) {
            if (image.getRefId() != null) {
                Deal deal = dealRepository.findById(image.getRefId()).orElse(null);
                if (deal != null && deal.getUserId().getUserId().equals(userDetails.userId())) {
                    isOwner = true;
                }
            }
        } else if (image.getImageType() == ImageType.USER) {
            if (image.getRefId() != null && image.getRefId().equals(userDetails.userId())) {
                isOwner = true;
            }
        }

        if (!isOwner && !isAdmin) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        // S3 삭제 및 DB 삭제
        s3Uploader.delete(image.getImageUrl());
        imageRepository.delete(image);

        return new ImageDeleteResponseDTO("이미지 삭제 완료");
    }

    // 임시 이미지 삭제
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    @Transactional
    public void deleteTempImages() {
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
    public Image getImageByUserId(Long userId) {

        var image = imageRepository.findByUserId(userId);
        return image.orElseGet(() -> Image.builder().imageId(null).imageUrl(null).build());
    }

    // 이미지 삭제
    @Transactional
    public ImageDeleteResponseDTO deleteImageByUserId(Long userId) {

        var image = imageRepository.findByUserId(userId);
        if(image.isEmpty()) return new ImageDeleteResponseDTO("해당 유저는 프로필 사진이 없습니다.");
        return deleteImage(image.map(Image::getRefId).orElseThrow(() -> new BaseException(ImageErrorCode.IMAGE_NOT_FOUND)));
    }


    // 크롤링용 매서드
    public List<Long> saveImageUrlsForCrawling(List<String> imgUrls) {

        List<Long> imageIds = new ArrayList<>();
        int cnt = 0;
        for(String imgUrl : imgUrls) {

            var img = Image.builder()
                    .imageIndex(cnt++)
                    .imageUrl(imgUrl)
                    .isTemp(true)
                    .build();

            imageIds.add(imageRepository.save(img).getImageId());
        }

        return imageIds;
    }


}
