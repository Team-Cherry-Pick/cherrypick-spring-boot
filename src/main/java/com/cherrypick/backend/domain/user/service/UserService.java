package com.cherrypick.backend.domain.user.service;


import com.cherrypick.backend.domain.user.dto.UserRequestDTOs;
import com.cherrypick.backend.domain.user.dto.UserResponseDTOs;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor @Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTOs.UpdateDTO userUpdate(UserRequestDTOs.UpdateDTO dto){

        // 인증된 유저의 ID를 찾음, 인증되지 않았다면 오류.
        var userId = AuthUtil.getUserDetail().userId();

        var user = userRepository.findById(userId).orElseThrow(() -> new BaseException(UserErrorCode.USER_NOT_FOUND));
        user.setNickname(dto.nickname());
        user.setEmail(dto.email());

        userRepository.save(user);


        return null;
    }

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

        log.info(nickname);
        userRepository.findAll().forEach(user -> {log.info(user.toString());});


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



}
