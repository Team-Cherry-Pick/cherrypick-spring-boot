package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.service.ImageService;
import com.cherrypick.backend.domain.user.dto.response.UserDetailResponseDTO;
import com.cherrypick.backend.domain.user.dto.request.UserUpdateRequestDTO;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.enums.Gender;
import com.cherrypick.backend.domain.user.enums.Role;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import com.cherrypick.backend.global.util.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ImageService imageService;
    
    @Mock
    private LogService logService;
    
    @InjectMocks
    private UserService userService;

    private User mockUser;
    private AuthenticatedUser mockAuthUser;
    private UserUpdateRequestDTO mockUpdateDto;
    private Image mockImage;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1L)
                .nickname("oldNickname")
                .birthday(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();

        mockAuthUser = AuthenticatedUser.builder()
                .userId(1L)
                .nickname("oldNickname")
                .role(Role.CLIENT)
                .build();

        mockUpdateDto = new UserUpdateRequestDTO(
                "newNickname",
                "1995-05-15",
                "FEMALE",
                2L
        );

        mockImage = Image.builder()
                .imageId(2L)
                .imageUrl("https://example.com/new-image.jpg")
                .build();
    }

    @Test
    @DisplayName("유저 정보 업데이트 성공")
    void userUpdate_Success() {
        // Given - 테스트에 필요한 데이터와 Mock 동작 설정
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            // 인증된 유저 정보 반환하도록 설정
            authUtil.when(AuthUtil::getUserDetail).thenReturn(mockAuthUser);
            
            // Repository와 Service Mock 동작 설정
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(imageService.updateUserProfileImage(eq(1L), eq(2L))).thenReturn(mockImage);

            // When - 실제 테스트할 메서드 실행
            UserDetailResponseDTO result = userService.userUpdate(mockUpdateDto);

            // Then - 결과 검증 및 Mock 호출 검증
            // 응답 객체가 null이 아님을 확인
            assertThat(result).isNotNull();
            
            // 유저 정보가 DTO 값으로 올바르게 업데이트되었는지 확인
            assertThat(mockUser.getNickname()).isEqualTo("newNickname");
            assertThat(mockUser.getBirthday()).isEqualTo(LocalDate.of(1995, 5, 15));
            assertThat(mockUser.getGender()).isEqualTo(Gender.FEMALE);
            
            // 예상된 메서드들이 정확한 인수로 호출되었는지 검증
            verify(userRepository).findById(1L);
            verify(userRepository).save(mockUser);
            verify(imageService).updateUserProfileImage(1L, 2L);
        }
    }

    @Test
    @DisplayName("존재하지 않는 유저 업데이트 시 예외 발생")
    void userUpdate_UserNotFound() {
        // Given - 존재하지 않는 유저 상황 설정
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            // 인증은 성공했지만, DB에는 해당 유저가 없는 상황
            authUtil.when(AuthUtil::getUserDetail).thenReturn(mockAuthUser);
            when(userRepository.findById(1L)).thenReturn(Optional.empty()); // 유저 없음

            // When & Then - 예외 발생 검증
            // UserNotFound 예외가 발생하는지 확인
            assertThatThrownBy(() -> userService.userUpdate(mockUpdateDto))
                    .isInstanceOf(BaseException.class)
                    .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

            // 예외 발생 후 불필요한 메서드가 호출되지 않았는지 검증
            verify(userRepository).findById(1L);
            verify(userRepository, never()).save(any()); // save 호출되면 안됨
            verify(imageService, never()).updateUserProfileImage(any(), any()); // 이미지 처리도 안됨
        }
    }

    @Test
    @DisplayName("이미지 없이 유저 정보만 업데이트")
    void userUpdate_WithoutImageChange() {
        // Given - 이미지 ID가 null인 업데이트 DTO 준비
        UserUpdateRequestDTO dtoWithoutImage = new UserUpdateRequestDTO(
                "newNickname",
                "1995-05-15", 
                "FEMALE",
                null  // 이미지 ID 없음 - 이미지는 변경하지 않음
        );
        
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            // Mock 설정 - 정상적인 유저 업데이트 플로우
            authUtil.when(AuthUtil::getUserDetail).thenReturn(mockAuthUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(imageService.updateUserProfileImage(eq(1L), eq(null))).thenReturn(mockImage);

            // When - 이미지 없는 업데이트 실행
            UserDetailResponseDTO result = userService.userUpdate(dtoWithoutImage);

            // Then - 기본 정보는 업데이트되고, 이미지 서비스에는 null이 전달됨
            assertThat(result).isNotNull();
            assertThat(mockUser.getNickname()).isEqualTo("newNickname");
            
            // ImageService에 null이 전달되어 이미지 처리 로직이 실행됨을 확인
            verify(imageService).updateUserProfileImage(1L, null);
        }
    }

    @Test
    @DisplayName("잘못된 날짜 형식으로 업데이트 시 예외 발생")
    void userUpdate_InvalidDateFormat() {
        // Given - 잘못된 날짜 형식을 가진 DTO 준비
        UserUpdateRequestDTO invalidDto = new UserUpdateRequestDTO(
                "newNickname",
                "invalid-date",  // 잘못된 날짜 형식 (YYYY-MM-DD 형식이 아님)
                "FEMALE",
                2L
        );
        
        try (MockedStatic<AuthUtil> authUtil = mockStatic(AuthUtil.class)) {
            // Mock 설정 - 유저는 존재하지만 날짜 파싱에서 실패할 상황
            authUtil.when(AuthUtil::getUserDetail).thenReturn(mockAuthUser);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            // When & Then - 날짜 파싱 예외 발생 검증
            // LocalDate.parse()에서 DateTimeParseException 발생 예상
            assertThatThrownBy(() -> userService.userUpdate(invalidDto))
                    .isInstanceOf(Exception.class);  // DateTimeParseException 또는 그 상위 예외
                    
            // 예외 발생으로 인해 save가 호출되지 않음을 확인
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    void getUserDetail() {
    }

    @Test
    void testGetUserDetail() {
    }

    @Test
    void nicknameValidation() {
    }

    @Test
    void softDelete() {
    }

    @Test
    void hardDelete() {
    }
}