package com.cherrypick.backend.domain.deal.adapter.in.web.reference;

import com.cherrypick.backend.domain.deal.application.dto.response.StoreResponseListDTO;
import com.cherrypick.backend.domain.deal.domain.service.reference.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController @Tag(name="Store")
@RequestMapping("/api") @RequiredArgsConstructor @Slf4j
public class StoreController
{
    private final StoreService storeService;

    @Operation(summary = "스토어 조회 API", description = "스토어를 조회합니다. 추후 캐싱 예정")
    @GetMapping("/store")
    public ResponseEntity<StoreResponseListDTO> getStoreList(@RequestParam(value = "version", defaultValue = "v1") String version)
    {
        return ResponseEntity.ok(storeService.getStoreList());
    }


}
