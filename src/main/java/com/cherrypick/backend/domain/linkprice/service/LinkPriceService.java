package com.cherrypick.backend.domain.linkprice.service;

import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.LinkPriceErrorCode;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class LinkPriceService {

    @Value("${linkprice.affiliate-id}")
    private String affiliateId;

    public String createDeeplink(String originalUrl) {
        // 원본 URL 유효성 체크
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new BaseException(LinkPriceErrorCode.INVALID_ORIGINAL_URL);
        }

        HttpURLConnection connection = null;
        try {
            // URL 인코딩
            String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);

            // API 요청 URL
            String apiUrl = "https://api.linkprice.com/ci/service/custom_link_xml"
                    + "?a_id=" + affiliateId
                    + "&url=" + encodedUrl
                    + "&mode=json";

            // 연결 설정
            connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            // HTTP 응답 코드 확인
            int status = connection.getResponseCode();
            if (status >= 400 && status < 500) {
                throw new BaseException(LinkPriceErrorCode.API_CLIENT_ERROR);
            } else if (status >= 500) {
                throw new BaseException(LinkPriceErrorCode.API_SERVER_ERROR);
            }

            // 응답 읽기
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "EUC-KR"))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }

                JSONObject json = new JSONObject(response.toString());

                // result 필드 체크
                if (!"S".equals(json.optString("result", ""))) {
                    throw new BaseException(LinkPriceErrorCode.LINKPRICE_API_RESULT_FAIL);
                }

                // 성공 시 딥링크 반환
                return json.getString("url");
            }

        } catch (java.net.SocketTimeoutException e) {
            throw new BaseException(LinkPriceErrorCode.API_TIMEOUT);
        } catch (java.net.UnknownHostException e) {
            throw new BaseException(LinkPriceErrorCode.API_HOST_UNREACHABLE);
        } catch (org.json.JSONException e) {
            throw new BaseException(LinkPriceErrorCode.INVALID_API_RESPONSE_FORMAT);
        } catch (BaseException be) {
            throw be; // 위에서 던진 것 그대로 전달
        } catch (Exception e) {
            throw new BaseException(LinkPriceErrorCode.LINKPRICE_API_EXCEPTION);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
