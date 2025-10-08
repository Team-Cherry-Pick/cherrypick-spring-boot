package com.cherrypick.backend.domain.deal.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    private String name;

    @ManyToMany(mappedBy = "discounts")
    private List<Deal> deals = new ArrayList<>();
}
