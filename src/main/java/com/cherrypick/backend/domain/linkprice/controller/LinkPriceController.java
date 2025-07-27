package com.cherrypick.backend.domain.linkprice.controller;

import com.cherrypick.backend.domain.linkprice.service.LinkPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/linkprice")
@RequiredArgsConstructor
public class LinkPriceController {

    private final LinkPriceService linkPriceService;

    // 테스트용 딥링크 변환 API
    // GET /api/test/linkprice?url=https://www.gmarket.co.kr
    @GetMapping
    public String testDeeplink(@RequestParam("url") String originalUrl) {
        return linkPriceService.createDeeplink(originalUrl);
    }
}
