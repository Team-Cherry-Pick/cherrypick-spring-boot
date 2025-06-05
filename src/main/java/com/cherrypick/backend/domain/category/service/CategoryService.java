package com.cherrypick.backend.domain.category.service;

import com.cherrypick.backend.domain.category.dto.response.CategoryListDTO;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리를 찾아서 리스트화해줌. 재귀함수 포함되어있음.
    // 비용이 크지는 않으나 Redis로 캐싱해줘도 좋을듯 함. (자주 바뀌는 요소가 아니니까, 쿼리 비용이 아까움.)
    public CategoryListDTO getCategories()
    {
        var categories = categoryRepository.findAll();

        ///  재귀함수
        var list = getCategoryListRecursive(0L, categories);

        return new CategoryListDTO(list);
    }

    // 계층을 타고 내려가서 각 카테고리를 리스트에 담음, 이후 상위 객체에 해당 리스트를 전달.
    public List<CategoryListDTO.CategoryDTO> getCategoryListRecursive(Long parentId ,List<Category> categories)
    {

        return categories.stream()
                .filter(c -> Optional.ofNullable(c.getParentId()).orElse(0L).equals(parentId))          // 해당 카테고리의 자식을 찾음
                .map(c -> CategoryListDTO.CategoryDTO.of(c, getCategoryListRecursive(c.getCategoryId(), categories)))// 자식을 불러와 리스트화 후 담음 (재귀적)
                .toList();
    }


}
