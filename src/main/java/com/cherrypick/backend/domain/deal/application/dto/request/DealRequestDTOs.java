package com.cherrypick.backend.domain.deal.application.dto.request;

import com.cherrypick.backend.domain.deal.domain.enums.UserBehaviorType;

import java.util.Map;

public class DealRequestDTOs {

    public record UserBehaviorDTO(
            Long userId,
            Long dealId,
            UserBehaviorType type
    ){
        public Map<String, String> toMap(){
            return Map.of("userId", String.valueOf(userId),
                        "dealId", String.valueOf(dealId),
                        "type", type.name()
            );
        }
    }

}
