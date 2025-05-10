package com.cherrypick.backend.domain.category.controller;

import com.cherrypick.backend.domain.category.dto.response.CategoryListDTO;
import com.cherrypick.backend.domain.category.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @Tag(name="카테고리", description = "카테고리 API")
@RequestMapping("/api") @RequiredArgsConstructor
public class CategoryController
{

    final CategoryService categoryService;

    @GetMapping("/category")
    public CategoryListDTO getCategories()
    {


        return categoryService.getCategories();
    }

}


