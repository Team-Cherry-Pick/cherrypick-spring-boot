package com.cherrypick.backend.domain.deal.domain.service;

import com.cherrypick.backend.global.log.domain.port.LogAppender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class DealLogService
{
    private final LogAppender logAppender;


    /**
     * 딜 상세 페이지에서 구매 버튼 클릭 이벤트 로그를 기록합니다.
     * <p>
     * 전환율 분석, 인기 카테고리 파악, 사용자별 구매 패턴 분석에 활용됩니다.
     * </p>
     *
     * @param userId 사용자 ID (비로그인 시 null)
     * @param deviceId 디바이스 고유 식별자
     * @param dealId 딜 ID
     * @param dealTitle 딜 제목
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리 이름
     */
    public void clickPurchaseLog(Long userId,
                                 String deviceId,
                                 Long dealId,
                                 String dealTitle,
                                 Long categoryId,
                                 String categoryName)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("dealId", Optional.ofNullable(dealId).orElse(-1L));
        map.put("dealTitle", Optional.ofNullable(dealTitle).orElse("unknown"));
        map.put("categoryId", Optional.ofNullable(categoryId).orElse(-1L));
        map.put("categoryName", Optional.ofNullable(categoryName).orElse("unknown"));

        logAppender.appendInfo("PURCHASE_CLICK_LOG", map);
    }

    /**
     * 딜 공유 버튼 클릭 이벤트 로그를 기록합니다.
     * <p>
     * 바이럴 확산 추이 분석, 공유율 높은 딜 파악, 사용자 참여도 측정에 활용됩니다.
     * </p>
     *
     * @param userId 사용자 ID (비로그인 시 null)
     * @param deviceId 디바이스 고유 식별자
     * @param dealId 딜 ID
     * @param dealTitle 딜 제목
     * @param categoryId 카테고리 ID
     * @param categoryName 카테고리 이름
     */
    public void clickShareLog(Long userId,
                              String deviceId,
                              Long dealId,
                              String dealTitle,
                              Long categoryId,
                              String categoryName)
    {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", Optional.ofNullable(userId).orElse(-1L));
        map.put("deviceId", Optional.ofNullable(deviceId).orElse("unknown"));
        map.put("dealId", Optional.ofNullable(dealId).orElse(-1L));
        map.put("dealTitle", Optional.ofNullable(dealTitle).orElse("unknown"));
        map.put("categoryId", Optional.ofNullable(categoryId).orElse(-1L));
        map.put("categoryName", Optional.ofNullable(categoryName).orElse("unknown"));

        logAppender.appendInfo("SHARE_CLICK_LOG", map);
    }

}
