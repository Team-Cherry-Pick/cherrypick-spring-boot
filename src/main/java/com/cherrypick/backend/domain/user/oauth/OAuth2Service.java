package com.cherrypick.backend.domain.user.oauth;

import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class OAuth2Service extends DefaultOAuth2UserService
{
    private final UserRepository userRepository;

    @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        var oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao 등

        // oauthId를 찾아냄.
        String oauthId = oauth2User.getAttribute("id").toString();
        var user = userRepository.findUserByOauthId(oauthId);

        User loginUser = null;
        if(user.isPresent()){
            // 저장된 유저 불러오기
            loginUser = user.get();
        }
        else{
            // 신규 유저 만들기, 추후 로그인 방법이 늘어나면 ENUM으로 처리하는 것도 고려.
            if(provider.equals("kakao")) loginUser = User.fromKakao(oauth2User);
            else throw new BaseException(UserErrorCode.UNDEFINED_OAUTH_PROVIDER);

            // 닉네임 만들기, 존재하는 닉네임이라면 다시 만들어줌.
            String newNickName = null;
            do{
                newNickName = loginUser.getNickname() + getRandomAdjective();
            }
            while(userRepository.findUserByNickname(newNickName).isPresent());

            loginUser.setNickname(newNickName);

            // 신규 유저 저장
            userRepository.save(loginUser);
        }

        // dto로 변환해 반환.
        return OAuth2UserDTO.from(loginUser);
    }

    @Override
    public void setAttributesConverter(Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        super.setAttributesConverter(attributesConverter);

    }


    public String getRandomAdjective()
    {
        var adjectiveList = List.of("멋있는 ", "대단한 ", "알뜰한 ", "네모난 ", "귀여운 ", "깜찍한 ", "듬직한 ", "깔롱한 ", "늠름한 ", "살뜰한 ", "짜릿한 ", "행복한 ", "소중한 ");
        int randomAdj = (int) Math.round(Math.random() * adjectiveList.size());

        return adjectiveList.get(randomAdj);
    }


}
