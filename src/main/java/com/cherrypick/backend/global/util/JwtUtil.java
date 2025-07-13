package com.cherrypick.backend.global.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component @Slf4j
public class JwtUtil
{
    public static String removeBearer(String token)
    {
        return token.replace("Bearer ", "");
    }
}