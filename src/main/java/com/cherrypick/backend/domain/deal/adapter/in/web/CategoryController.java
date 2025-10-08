package com.cherrypick.backend.domain.deal.adapter.in.web;

import com.cherrypick.backend.domain.deal.application.dto.response.CategoryListDTO;
import com.cherrypick.backend.domain.deal.domain.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController @Tag(name="Category", description = "카테고리 API")
@RequestMapping("/api") @RequiredArgsConstructor
public class CategoryController
{
    private final CategoryService categoryService;

    @Operation(
            summary = "카테고리 조회 API V1",
            description = "전체 카테고리를 조회합니다. 계층별로 내려가며 카테고리를 전달합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "카테고리 조회 성공."),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/category")
    public ResponseEntity<CategoryListDTO> getCategories(@RequestParam(value = "version", defaultValue = "v1") String version)
    {
        return ResponseEntity.ok(categoryService.getCategories());
    }


}


