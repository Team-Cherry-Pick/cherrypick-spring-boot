package com.cherrypick.backend.domain.deal.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Filter {

    private boolean viewSoldOut;      // 품절 포함 여부
    private boolean freeShipping;     // 무료배송 여부
    private boolean globalShipping;   // 해외직구 여부
}
