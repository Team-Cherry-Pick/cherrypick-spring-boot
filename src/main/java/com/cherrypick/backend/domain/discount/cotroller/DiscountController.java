package com.cherrypick.backend.domain.discount.cotroller;

import com.cherrypick.backend.domain.discount.dto.DiscountResponseListDTO;
import com.cherrypick.backend.domain.discount.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
