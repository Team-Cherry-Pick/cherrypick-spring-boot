package com.cherrypick.backend.domain.hashtag.entity;

import com.cherrypick.backend.domain.deal.entity.Deal;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity @Getter @Setter @ToString
@AllArgsConstructor @NoArgsConstructor
@Builder
public class DealTag
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dealTagId;

    // 진짜 DB 컬럼 매핑 (ID만 들고 있는 필드)
    @Column(name = "deal_id", nullable = false)
    private Long dealId;

    @Column(name = "hash_tag_id", nullable = false)
    private Long hashTagId;


    // 쿼리 최적화를 위해 객체는 분리.
    @ManyToOne(fetch = FetchType.LAZY) @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "deal_id", insertable = false, updatable = false)
    private Deal deal;

    @ManyToOne(fetch = FetchType.LAZY) @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "hash_tag_id", insertable = false, updatable = false)
    private HashTag hashTag;

}

