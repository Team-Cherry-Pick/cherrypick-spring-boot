package com.cherrypick.backend.global.util;

public class CacheKeyUtil
{
    private CacheKeyUtil(){}

    public static String getRefreshTokenKey(Long userId, String deviceId){
        return String.format("user:%s:token:refresh:%s", userId.toString(), deviceId);
    }

}
