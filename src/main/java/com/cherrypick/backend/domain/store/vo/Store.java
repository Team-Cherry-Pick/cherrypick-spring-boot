package com.cherrypick.backend.domain.store.vo;

// TODO : Store 엔티티와 파일명이 겹침. 수정 요함.
public record Store(
        Long storeId,
        String storeName,
        String textColor,
        String backgroundColor
) {
}
