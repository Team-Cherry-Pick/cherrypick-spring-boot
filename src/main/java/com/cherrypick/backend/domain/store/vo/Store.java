package com.cherrypick.backend.domain.store.vo;

import jakarta.persistence.Embeddable;

@Embeddable
public class Store {

    private String storeName;
    private String textColor;
    private String backgroundColor;
}
