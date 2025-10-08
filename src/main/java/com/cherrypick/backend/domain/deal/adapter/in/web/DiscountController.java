package com.cherrypick.backend.domain.deal.adapter.in.web;

import com.cherrypick.backend.domain.deal.application.dto.response.DiscountResponseListDTO;
import com.cherrypick.backend.domain.deal.domain.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="Discount")
@RestController@RequiredArgsConstructor @Log4j2 @RequestMapping("/api")
public class DiscountController
{
    private final DiscountService discountService;

    @Operation(summary = "할인 정보 조회 API"
            , description = "할인 정보를 조회합니다.")
    @GetMapping("/discount")
    public ResponseEntity<DiscountResponseListDTO> getDiscountList(){

        return ResponseEntity.ok(discountService.getDiscountList());
    }

}
