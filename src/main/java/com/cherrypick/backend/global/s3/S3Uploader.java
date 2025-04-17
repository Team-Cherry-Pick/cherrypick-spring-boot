package com.cherrypick.backend.global.s3;

import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.ImageErrorCode;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String folderName) {
        String fileName = folderName + "/" + UUID.randomUUID() + ".jpg";

        byte[] compressedImage = compressHighQuality(file);  // 내부에서 에러 처리

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(compressedImage)
            );
        } catch (Exception e) {
            throw new BaseException(ImageErrorCode.IMAGE_S3_PUT_FAILED);
        }

        try {
            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder().bucket(bucket).key(fileName).build())
                    .toString();
        } catch (Exception e) {
            throw new BaseException(ImageErrorCode.IMAGE_S3_URL_FAILED);
        }
    }

    private byte[] compressHighQuality(MultipartFile file) {
        BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new BaseException(ImageErrorCode.IMAGE_READ_FAILED);
        }

        if (originalImage == null) {
            throw new BaseException(ImageErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(2000, 2000) // 이미지 비율 Thumbnailator에서 유지됨
                    .outputFormat("jpg")
                    .outputQuality(0.9f)
                    .toOutputStream(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new BaseException(ImageErrorCode.IMAGE_COMPRESS_FAILED);
        }
    }
}
