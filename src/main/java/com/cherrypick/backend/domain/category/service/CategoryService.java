package com.cherrypick.backend.domain.category.service;

import com.cherrypick.backend.domain.category.dto.response.CategoryListDTO;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryListDTO getCategories()
    {
        var categories = categoryRepository.findAll();
        //categories.stream().max(c -> c)

        return null;
    }


}
