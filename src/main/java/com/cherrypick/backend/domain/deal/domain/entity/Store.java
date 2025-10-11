package com.cherrypick.backend.domain.deal.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storeId;

    private String name;
    private boolean isAffiliate;
    private String backgroundColor;
    private String textColor;
    private String storeRank;
    private boolean isBlind = false;

}
