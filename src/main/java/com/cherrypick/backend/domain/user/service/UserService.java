package com.cherrypick.backend.domain.user.service;


import com.cherrypick.backend.domain.user.dto.UserResponseDTOs;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor @Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTOs.nicknameValid nicknameValidation(String nickname) {

        if(nickname.length() < 2) {
            return UserResponseDTOs.nicknameValid.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("닉네임은 2글자 이상이어야 합니다.")
                    .build();
        }

        if(20 < nickname.length()) {
            return UserResponseDTOs.nicknameValid.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("닉네임은 20자를 넘길 수 없습니다.")
                    .build();
        }

        log.info(nickname);
        userRepository.findAll().forEach(user -> {log.info(user.toString());});


        if(userRepository.findUserByNickname(nickname).isPresent())
        {
            return UserResponseDTOs.nicknameValid.builder()
                    .nickname(nickname)
                    .isValid(false)
                    .details("이미 존재하는 닉네임입니다.")
                    .build();
        }

        return UserResponseDTOs.nicknameValid.builder()
                .nickname(nickname)
                .isValid(true)
                .details("사용하실 수 있는 닉네임입니다!")
                .build();

    }

}
