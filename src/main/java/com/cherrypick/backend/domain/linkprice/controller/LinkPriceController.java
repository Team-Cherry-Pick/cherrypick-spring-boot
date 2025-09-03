package com.cherrypick.backend.domain.linkprice.controller;

import com.cherrypick.backend.domain.linkprice.dto.request.LinkPriceRequest;
import com.cherrypick.backend.domain.linkprice.dto.response.LinkPriceResponse;
import com.cherrypick.backend.domain.linkprice.service.LinkPriceService;
import com.cherrypick.backend.domain.linkprice.service.LinkPriceTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class LinkPriceController {

    private final LinkPriceService linkPriceService;
    private final LinkPriceTransactionService transactionService;

    // 테스트용 딥링크 변환 API
    // GET /api/test/linkprice?url=https://www.gmarket.co.kr
    @GetMapping("/api/test/linkprice")
    public String testDeeplink(@RequestParam("url") String originalUrl) {
        return linkPriceService.createDeeplink(originalUrl);
    }

    // 딥링크 리다이렉션 API
    @GetMapping("/api/redirect/{dealId}")
    public RedirectView redirect(@PathVariable Long dealId, HttpServletRequest request) {
        return linkPriceService.redirectToDeeplink(dealId, request);
    }

    // 실적조회 API
    @PostMapping("/api/linkprice/transactions")
    public LinkPriceResponse getTransactions(@RequestBody LinkPriceRequest request) {
        return transactionService.getTransactions(request);
    }
}
