package com.cherrypick.backend.domain.deal.dto.response;

public class DealResponseDTOs {

    public record Create(Long dealId, String message){}
    public record Update(Long dealId, String message){}
    public record Delete(String message){}
}
