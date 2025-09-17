package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import com.cherrypick.backend.domain.deal.adapter.out.RedisDuplicationPreventionAdapter;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.util.AuthUtil;
import com.cherrypick.backend.global.util.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLOutput;

@Slf4j
@Service @RequiredArgsConstructor
public class DealLogService
{
    private final DealRepository dealRepository;
    private final CategoryRepository categoryRepository;
    private final LogService logService;
    private final RedisDuplicationPreventionAdapter duplicationPreventionAdapter;

    @Transactional
    public String putPurchaseClickLog(Long dealId, String deviceId)
    {
        // 딜 구매버튼 클릭 시
        if(duplicationPreventionAdapter.isDuplicate(RedisDuplicationPreventionAdapter.Behavior.PURCHASE, dealId, deviceId))
        {
            return "DUPLICATE";
        }
        duplicationPreventionAdapter.preventDuplicate(RedisDuplicationPreventionAdapter.Behavior.PURCHASE, dealId, deviceId);

        var deal = dealRepository.findById(dealId).orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));
        var category = deal.getCategoryId();
        Long user = null;
        try{
            user = AuthUtil.getUserDetail().userId();
        } catch (RuntimeException e) {
            System.out.println("등록되지 않은 유저");
        }

        // 구매 버튼을 눌렀으니 가중치를 2 추가
        dealRepository.updateHeat(dealId, 2.0);
        logService.clickPurchaseLog(
                user,
                deviceId,
                deal.getDealId(),
                deal.getTitle(),
                category.getCategoryId(),
                category.getName()
        );

        return "success";
    }

    @Transactional
    public String putShareClickLog(Long dealId, String deviceId)
    {
        // 딜 구매버튼 클릭 시
        if(duplicationPreventionAdapter.isDuplicate(RedisDuplicationPreventionAdapter.Behavior.SHARE, dealId, deviceId))
        {
            return "DUPLICATE";
        }
        duplicationPreventionAdapter.preventDuplicate(RedisDuplicationPreventionAdapter.Behavior.SHARE, dealId, deviceId);

        var deal = dealRepository.findById(dealId).orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));
        var category = deal.getCategoryId();
        Long user = null;
        try{
            user = AuthUtil.getUserDetail().userId();
        } catch (RuntimeException e) {
            System.out.println("등록되지 않은 유저");
        }

        // 구매 버튼을 눌렀으니 가중치를 2 추가
        dealRepository.updateHeat(dealId, 2.0);
        logService.clickShareLog(
                user,
                deviceId,
                deal.getDealId(),
                deal.getTitle(),
                category.getCategoryId(),
                category.getName()
        );

        return "success";
    }



}
