package com.cherrypick.backend.domain.deal.application.dto.response;

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
