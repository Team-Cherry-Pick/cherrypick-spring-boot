package com.cherrypick.backend.domain.linkprice.controller;

import com.cherrypick.backend.domain.linkprice.dto.request.LinkPriceRequest;
import com.cherrypick.backend.domain.linkprice.dto.response.LinkPriceResponse;
import com.cherrypick.backend.domain.linkprice.entity.LinkPriceTransaction;
import com.cherrypick.backend.domain.linkprice.repository.LinkPriceTransactionRepository;
import com.cherrypick.backend.domain.linkprice.service.LinkPriceTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LinkPriceSchedulers {

    private final LinkPriceTransactionService linkPriceService;
    private final LinkPriceTransactionRepository txRepo;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int PER_PAGE = 1000; // 한 번에 받아올 건수

    // 매일 호출: DB에 전부 저장
    @Scheduled(cron = "0 10 3 * * *", zone = "Asia/Seoul")
    public void collectDaily() {
        LocalDate target = LocalDate.now(KST).minusDays(1);
        fetchDay(target, UpdateMode.FULL);
    }

    // 매달 6일: 익익월 변경된 실적 업서트
    @Scheduled(cron = "0 10 4 6 * *", zone = "Asia/Seoul")
    public void reconcileMonthly() {
        YearMonth ym = YearMonth.now(KST).minusMonths(2);
        for (LocalDate d = ym.atDay(1); !d.isAfter(ym.atEndOfMonth()); d = d.plusDays(1)) {
            fetchDay(d, UpdateMode.DIFF); // 바뀐 게 있으면 갱신
        }
    }

    // fetchDay 모드 -> 전체 저장 or 바뀐 것 저장
    private enum UpdateMode { FULL, DIFF }

    // 특정 날짜 업서트 메소드
    private void fetchDay(LocalDate day, UpdateMode mode) {
        String yyyymmdd = day.format(YMD);
        int page = 1;

        while (true) {
            LinkPriceRequest req = LinkPriceRequest.builder()
                    .yyyymmdd(yyyymmdd)
                    .page(page)
                    .perPage(PER_PAGE)
                    .build();

            LinkPriceResponse res = linkPriceService.getTransactions(req);
            List<LinkPriceResponse.Order> items = (res == null) ? null : res.order_list();
            if (items == null || items.isEmpty()) break;

            LocalDateTime now = LocalDateTime.now(KST);

            for (LinkPriceResponse.Order o : items) {
                String id = o.trlog_id();
                if (id == null || id.isBlank()) continue;

                LinkPriceTransaction tx = txRepo.findById(id)
                        .orElseGet(() -> LinkPriceTransaction.builder().trlogId(id).build());

                int newStatus = parseInt(o.status());

                if (mode == UpdateMode.DIFF) {
                    // 달라진 필드만 저장
                    boolean changed = false;

                    changed |= set(tx.getStatus(),             newStatus,                   tx::setStatus);
                    changed |= set(tx.getTransComment(),       o.trans_comment(),           tx::setTransComment);
                    changed |= set(tx.getSales(),              bd(o.sales()),               tx::setSales);
                    changed |= set(tx.getCommission(),         bd(o.commission()),          tx::setCommission);
                    changed |= set(tx.getProgramName(),        o.pgm_name(),                tx::setProgramName);
                    changed |= set(tx.getDevice(),             o.is_pc(),                   tx::setDevice);
                    changed |= set(tx.getPurchaseRate(),       o.pur_rate(),                tx::setPurchaseRate);
                    changed |= set(tx.getCategoryCode(),       o.c_cd(),                    tx::setCategoryCode);
                    changed |= set(tx.getOrderYyyymmdd(),      o.yyyymmdd(),                tx::setOrderYyyymmdd);
                    changed |= set(tx.getOrderHhmiss(),        o.hhmiss(),                  tx::setOrderHhmiss);
                    changed |= set(tx.getCreateDate(),         parseYmd(o.create_time_stamp()), tx::setCreateDate);
                    changed |= set(tx.getMerchantId(),         o.m_id(),                    tx::setMerchantId);
                    changed |= set(tx.getOrderNo(),            o.o_cd(),                    tx::setOrderNo);
                    changed |= set(tx.getProductCode(),        o.p_cd(),                    tx::setProductCode);
                    changed |= set(tx.getProductName(),        o.p_nm(),                    tx::setProductName);
                    changed |= set(tx.getItemCount(),          parseInt(o.it_cnt()),        tx::setItemCount);
                    changed |= set(tx.getAppliedProgramId(),   o.applied_pgm_id(),          tx::setAppliedProgramId);
                    changed |= set(tx.getAffiliateUserId(),    o.user_id(),                 tx::setAffiliateUserId);
                    // 통화/수집시각
                    changed |= set(tx.getCurrency(),           "KRW",                       tx::setCurrency);

                    if (changed) {
                        tx.setFetchedAt(now);
                        txRepo.save(tx);
                    }
                    continue;
                }

                // FULL 모드: 모든 컬럼 업서트
                tx.setMerchantId(o.m_id());
                tx.setOrderNo(o.o_cd());
                tx.setProductCode(o.p_cd());
                tx.setProductName(o.p_nm());
                tx.setItemCount(parseInt(o.it_cnt()));
                tx.setAffiliateUserId(o.user_id());
                tx.setStatus(newStatus);
                tx.setCategoryCode(o.c_cd());
                tx.setCreateDate(parseYmd(o.create_time_stamp()));
                tx.setAppliedProgramId(o.applied_pgm_id());
                tx.setOrderYyyymmdd(o.yyyymmdd());
                tx.setOrderHhmiss(o.hhmiss());
                tx.setTransComment(o.trans_comment());
                tx.setSales(bd(o.sales()));
                tx.setCommission(bd(o.commission()));
                tx.setProgramName(o.pgm_name());
                tx.setDevice(o.is_pc());
                tx.setPurchaseRate(o.pur_rate());
                tx.setCurrency("KRW");
                tx.setFetchedAt(now);
                txRepo.save(tx);
            }

            if (items.size() < PER_PAGE) break;
            page++;
        }
    }
    private static java.math.BigDecimal bd(double v) { return new java.math.BigDecimal(Double.toString(v)); }
    private static java.math.BigDecimal bd(int v)    { return new java.math.BigDecimal(Integer.toString(v)); }

    private <T> boolean set(T oldVal, T newVal, java.util.function.Consumer<T> setter) {
        if (!java.util.Objects.equals(oldVal, newVal)) { setter.accept(newVal); return true; }
        return false;
    }
    private boolean set(java.math.BigDecimal oldVal, java.math.BigDecimal newVal, java.util.function.Consumer<java.math.BigDecimal> setter) {
        boolean diff = (oldVal == null && newVal != null) || (oldVal != null && newVal == null)
                || (oldVal != null && newVal != null && oldVal.compareTo(newVal) != 0);
        if (diff) { setter.accept(newVal); return true; }
        return false;
    }
    private Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }
    private LocalDate parseYmd(String yyyymmdd) {
        try { return (yyyymmdd == null || yyyymmdd.isBlank()) ? null : LocalDate.parse(yyyymmdd, YMD); }
        catch (Exception e) { return null; }
    }
}
