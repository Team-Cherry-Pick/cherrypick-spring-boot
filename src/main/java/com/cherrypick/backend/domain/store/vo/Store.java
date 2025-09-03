package com.cherrypick.backend.domain.store.vo;

public record Store(
        Long storeId,
        String storeName,
        String textColor,
        String backgroundColor
) {
}
