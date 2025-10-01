package com.cherrypick.backend.domain.deal.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

public class ValidUrlUtil {

    // URL 검증
    public static boolean isValidUrl(String url) {
        Set<String> blockedShortDomains = Set.of(
                "bit.ly", "t.co", "goo.gl", "tinyurl.com", "is.gd",
                "ow.ly", "buff.ly", "cutt.ly", "rebrand.ly", "shorturl.at",
                "adf.ly", "lnkd.in"
        );

        if (url == null || url.isBlank()) return false;

        try {
            URI uri = new URI(url);

            // http 또는 https만 허용
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            // 호스트 검증
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            // 단축 URL 도메인 차단
            if (blockedShortDomains.contains(host.toLowerCase())) {
                return false;
            }

            return true;

        } catch (URISyntaxException e) {
            return false;
        }
    }


}
