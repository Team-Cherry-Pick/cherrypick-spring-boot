package com.cherrypick.backend.domain.deal.vo;

public record Filter (

        boolean viewSoldOut,        // 품절 포함 여부
        boolean freeShipping,       // 무료배송 여부
        boolean globalShipping      // 해외직구 여부
){
}
