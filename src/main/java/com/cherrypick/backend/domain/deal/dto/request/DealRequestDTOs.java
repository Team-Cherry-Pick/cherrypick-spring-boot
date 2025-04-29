package com.cherrypick.backend.domain.deal.dto.request;

import java.util.Map;

public class DealRequestDTOs {

    public record UserBehaviorDTO(
            Long userId,
            Long dealId,
            String method
    ){
        public Map<String, String> toMap(){
            return Map.of("userId", String.valueOf(userId),
                        "dealId", String.valueOf(dealId),
                        "method", method
            );
        }
    }




}
