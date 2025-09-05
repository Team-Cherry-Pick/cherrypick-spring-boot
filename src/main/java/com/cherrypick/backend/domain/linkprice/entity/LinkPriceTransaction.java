package com.cherrypick.backend.domain.linkprice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkPriceTransaction {

    @Id
    @Column(length = 20)                 // trlog_id
    private String trlogId;

    @Column(length = 20)
    private String merchantId;           // m_id

    @Column(length = 100)
    private String orderNo;              // o_cd

    @Column(length = 100)
    private String productCode;          // p_cd

    @Column(length = 300)
    private String productName;          // p_nm

    private Integer itemCount;           // it_cnt

    @Column(length = 560)
    private String affiliateUserId;      // user_id (u_id)

    private Integer status;              // 100/200/210/300/310

    @Column(length = 200)
    private String categoryCode;         // c_cd

    private LocalDate createDate;        // create_time_stamp (YYYYMMDD)

    @Column(length = 10)
    private String appliedProgramId;     // applied_pgm_id

    @Column(length = 8)
    private String orderYyyymmdd;        // yyyymmdd

    @Column(length = 6)
    private String orderHhmiss;          // hhmiss

    @Column(length = 1000)
    private String transComment;         // trans_comment

    @Column(precision = 19, scale = 4)
    private BigDecimal sales;            // sales

    @Column(precision = 19, scale = 4)
    private BigDecimal commission;       // commission

    @Column(length = 100)
    private String programName;          // pgm_name

    @Column(length = 20)
    private String device;               // is_pc (mobile|pc|app|ios|android)

    @Column(length = 50)
    private String purchaseRate;         // pur_rate

    @Column(length = 3)
    private String currency;             // 요청에 사용한 통화 (KRW, USD)

    private LocalDateTime fetchedAt;     // 저장 시각 (KST 기준)
}
