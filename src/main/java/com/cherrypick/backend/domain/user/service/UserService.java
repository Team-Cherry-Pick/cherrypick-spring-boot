package com.cherrypick.backend.domain.user.service;


import com.cherrypick.backend.domain.deal.dto.request.DealRequestDTOs;
import com.cherrypick.backend.domain.image.enums.ImageType;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.user.dto.UserDetailResponseDTO;
import com.cherrypick.backend.domain.user.dto.UserRequestDTOs;
import com.cherrypick.backend.domain.user.dto.UserResponseDTOs;
import com.cherrypick.backend.domain.user.dto.UserUpdateRequestDTO;
import com.cherrypick.backend.domain.user.enums.UserStatus;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor @Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    @Transactional
    public UserDetailResponseDTO userUpdate(UserUpdateRequestDTO dto){

        // 인증된 유저의 ID를 찾음, 인증되지 않았다면 오류.
        var userId = AuthUtil.getUserDetail().userId();
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));

        imageService.deleteImageByUserId(userId);
        imageService.attachImage(userId, List.of(dto.imageId()), ImageType.USER);

        user.setNickname(dto.nickname());
        user.setEmail(dto.email());

        var updatedUser = userRepository.save(user);
        var profileImage = imageService.getImageByUserId(userId);

        return UserDetailResponseDTO.from(updatedUser, profileImage);
    }

    @Transactional(readOnly = true)
    public UserDetailResponseDTO getUserDetail()
    {
        Long userId = AuthUtil.getUserDetail().userId();
        return getUserDetail(userId);
    }

    @Transactional(readOnly = true)
    public UserDetailResponseDTO getUserDetail(Long userId)
    {
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        var profileImage = imageService.getImageByUserId(userId);

        return UserDetailResponseDTO.from(user, profileImage);
    }

    @Transactional(readOnly = true)
    public UserResponseDTOs.NicknameValidDTO nicknameValidation(String nickname) {

        if(nickname.length() < 2) {
            return UserResponseDTOs.NicknameValidDTO.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("닉네임은 2글자 이상이어야 합니다.")
                    .build();
        }

        if(20 < nickname.length()) {
            return UserResponseDTOs.NicknameValidDTO.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("닉네임은 20자를 넘길 수 없습니다.")
                    .build();
        }

        if(userRepository.findUserByNickname(nickname).isPresent())
        {
            return UserResponseDTOs.NicknameValidDTO.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("이미 존재하는 닉네임입니다.")
                    .build();
        }

        return UserResponseDTOs.NicknameValidDTO.builder()
                .nickname(nickname)
                .isValid(true)
                .details("사용하실 수 있는 닉네임입니다!")
                .build();

    }

    public UserResponseDTOs.DeleteResponseDTO softDelete(UserRequestDTOs.DeleteRequestDTO dto) {

        var userId = AuthUtil.getUserDetail().userId();
        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        user.setStatus(UserStatus.DEACTIVATED);
        var deletedUserId = userRepository.save(user);

        return new UserResponseDTOs.DeleteResponseDTO(deletedUserId.getUserId(), "soft delete success");
    }

    /// 매일 오전 4시 정각에 실행, 성능 생각하면 리펙토링 해야함.
    /// 하드 딜리트하는 함수
    @Scheduled(cron = "0 0 4 * * *")
    public void hardDelete() {

        var userList = userRepository.findDeactivatedUsers();
        userRepository.deleteAll(userList);
        log.info("{} ::::: 총 {}명의 유저 삭제", LocalDateTime.now(), userList.size());
    }

}
