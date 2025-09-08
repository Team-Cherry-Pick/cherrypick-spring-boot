package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.auth.domain.vo.AuthenticatedUser;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
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

import java.sql.SQLOutput;

@Slf4j
@Service @RequiredArgsConstructor
public class DealLogService
{
    private final DealRepository dealRepository;
    private final CategoryRepository categoryRepository;
    private final LogService logService;

    public String putPurchaseClickLog(Long dealId, String deviceId)
    {
        var deal = dealRepository.findById(dealId).get();
        var category = deal.getCategoryId();
        Long user = null;
        try{
            user = AuthUtil.getUserDetail().userId();
        } catch (RuntimeException e) {
            System.out.println("등록되지 않은 유저");
        }

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


}
