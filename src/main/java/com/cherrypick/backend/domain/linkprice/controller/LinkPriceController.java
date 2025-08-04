package com.cherrypick.backend.domain.linkprice.controller;

import com.cherrypick.backend.domain.linkprice.service.LinkPriceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequiredArgsConstructor
public class LinkPriceController {

    private final LinkPriceService linkPriceService;

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
}
