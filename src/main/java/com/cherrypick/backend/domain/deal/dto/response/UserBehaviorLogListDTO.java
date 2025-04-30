package com.cherrypick.backend.domain.deal.dto.response;

import com.cherrypick.backend.domain.deal.enums.UserBehaviorType;
import lombok.Builder;

import java.util.List;

@Builder
public record UserBehaviorLogListDTO(

        List<UserBehaviorLogDTO> userBehaviorLogs

) {
    @Builder
    public record UserBehaviorLogDTO(
            String logId,
            String targetDealId,
            String targetDealTitle,
            String type,
            String createdAt
    ){

    }
}
