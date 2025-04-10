package com.cherrypick.backend.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service @RequiredArgsConstructor @Slf4j
public class OAuth2Service extends DefaultOAuth2UserService
{
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("::::loadUser::::");
        var oauth2User = super.loadUser(userRequest);
        System.out.println(oauth2User.getAttributes());

        return oauth2User;
    }

    @Override
    public void setAttributesConverter(Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
        super.setAttributesConverter(attributesConverter);



    }
}
