package com.cherrypick.backend.domain.user.service;

import com.cherrypick.backend.global.log.domain.port.LogAppender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class UserLogService {
    private final LogAppender logAppender;
    /**
     * 사용자 회원 탈퇴 이벤트 로그를 기록합니다.
     * <p>
     * GDPR 및 개인정보보호법 준수를 위해 탈퇴 사용자 정보를 추적합니다.
     * </p>
     *
     * @param userId 탈퇴 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param oauthId OAuth 제공자 고유 ID
     * @param message 탈퇴 사유 또는 추가 메시지
     */
    public void userDeleteLog(Long userId, String name, String email, String oauthId, String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("deluser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("deluser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("deluser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("deluser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("deluser_email", Optional.ofNullable(email).orElse("unknown"));

        logAppender.appendInfo("USER_DELETE_LOG", map);
    }

    /**
     * 신규 사용자 회원가입 이벤트 로그를 기록합니다.
     * <p>
     * 가입자 유입 경로 분석 및 사용자 증가 추이 파악에 활용됩니다.
     * </p>
     *
     * @param userId 신규 사용자 ID
     * @param name 사용자 이름
     * @param email 사용자 이메일
     * @param oauthId OAuth 제공자 고유 ID
     * @param message 가입 경로 또는 추가 메시지
     */
    public void userRegisterLog(Long userId, String name, String email, String oauthId, String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("reguser_msg", Optional.ofNullable(message).orElse("unknown"));
        map.put("reguser_id", Optional.ofNullable(userId).orElse(-1L));
        map.put("reguser_name", Optional.ofNullable(name).orElse("unknown"));
        map.put("reguser_oauthid", Optional.ofNullable(oauthId).orElse("unknown"));
        map.put("reguser_email", Optional.ofNullable(email).orElse("unknown"));

        logAppender.appendInfo("USER_REGISTER_LOG", map);
    }
}
