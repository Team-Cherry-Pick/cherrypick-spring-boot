package com.cherrypick.backend.domain.deal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@Setter @ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class HashTag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hashTagId;

    @Unique
    String name;

    @OneToMany(mappedBy = "hashTag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DealTag> dealTags = new ArrayList<>();

}
