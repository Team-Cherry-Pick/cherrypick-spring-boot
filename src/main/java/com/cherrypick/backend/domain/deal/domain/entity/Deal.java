package com.cherrypick.backend.domain.deal.domain.entity;

import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.deal.domain.entity.vo.Price;
import com.cherrypick.backend.domain.deal.domain.entity.vo.Shipping;
import com.cherrypick.backend.domain.discount.entity.Discount;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String originalUrl;

    @Column(length = 500)
    private String deepLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Embedded
    private Price price;

    @Embedded
    private Shipping shipping;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "deal_discount",
            joinColumns = @JoinColumn(name = "deal_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id"),
            foreignKey = @ForeignKey(
                    name = "FK_deal_discount_deal",
                    foreignKeyDefinition = "FOREIGN KEY (deal_id) REFERENCES deal(deal_id) ON DELETE CASCADE"
            )
    )
    private List<Discount> discounts = new ArrayList<>();

    private boolean isSoldOut;

    private String storeName;
    private String discountName;
    private String discountDescription;

    @Column(nullable = false)
    private double heat = 0.0;

    @Builder.Default
    private Long totalViews = 0L;

    @Builder.Default
    private Boolean isDelete = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 딜의 인기도(heat)를 업데이트합니다.
     *
     * @param amount 변경할 인기도 값 (양수: 증가, 음수: 감소)
     * @return 업데이트된 인기도 값 (-999.0 ~ 999.0 범위로 제한됨)
     */
    public Double updateHeat(double amount){

        var updatedHeat = heat + amount;
        if(updatedHeat < -999.0) updatedHeat = -999.0;
        else if(updatedHeat > 999.0) updatedHeat = 999.0;

        return updatedHeat;
    }



}
