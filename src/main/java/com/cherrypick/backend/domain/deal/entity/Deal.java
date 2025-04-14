package com.cherrypick.backend.domain.deal.entity;

import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.deal.vo.Shipping;
import com.cherrypick.backend.domain.discount.entity.Discount;
import com.cherrypick.backend.domain.store.entity.Store;
import com.cherrypick.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
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
    @JoinColumn(name = "user_id")
    private User userId;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category categoryId;

    private String originalUrl;
    private String deepLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store storeId;

    @Embedded
    private Price price;

    @Embedded
    private Shipping shipping;

    private String content;

    @ManyToMany
    @JoinTable(
            name = "deal_discount",
            joinColumns = @JoinColumn(name = "deal_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    private List<Discount> discounts = new ArrayList<>();

    private boolean isSoldOut;

    private String storeName;
    private String discountName;

    private boolean isDelete;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
