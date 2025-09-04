package com.cherrypick.backend.domain.category.service;

import com.cherrypick.backend.domain.category.dto.response.CategoryListDTO;
import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service @RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 카테고리를 찾아서 리스트화해줌. 재귀함수 포함되어있음.
    // 비용이 크지는 않으나 Redis로 캐싱해줘도 좋을듯 함. (자주 바뀌는 요소가 아니니까, 쿼리 비용이 아까움.)
    public CategoryListDTO getCategories()
    {

        var categoriesDto = redisTemplate.opsForValue().get("cache:categories");

        if(categoriesDto == null)
        {
            ///  재귀함수
            var categories = categoryRepository.findAll();
            var list = getCategoryListRecursive(0L, categories);
            categoriesDto = new CategoryListDTO(list);
            redisTemplate.opsForValue().set("cache:categories", categoriesDto, 1, TimeUnit.DAYS);
        }

        return (CategoryListDTO) categoriesDto;
    }

    // 계층을 타고 내려가서 각 카테고리를 리스트에 담음, 이후 상위 객체에 해당 리스트를 전달.
    public List<CategoryListDTO.CategoryDTO> getCategoryListRecursive(Long parentId ,List<Category> categories)
    {

        return categories.stream()
                .filter(c -> Optional.ofNullable(c.getParentId()).orElse(0L).equals(parentId))          // 해당 카테고리의 자식을 찾음
                .map(c -> CategoryListDTO.CategoryDTO.of(c, getCategoryListRecursive(c.getCategoryId(), categories)))// 자식을 불러와 리스트화 후 담음 (재귀적)
                .toList();
    }

    // 특정 카테고리와 그 하위 카테고리들의 ID를 모두 조회
    public List<Long> getCategoryWithChildren(Long categoryId) {
        List<Category> allCategories = categoryRepository.findAll();
        List<Long> result = new ArrayList<>();
        
        // 해당 카테고리 찾기
        Optional<Category> targetCategory = allCategories.stream()
                .filter(c -> c.getCategoryId().equals(categoryId))
                .findFirst();
        
        if (targetCategory.isPresent()) {
            result.add(targetCategory.get().getCategoryId());
            // 하위 카테고리들 재귀적으로 추가
            addChildCategoryIds(categoryId, allCategories, result);
        }
        
        return result;
    }

    // 재귀적으로 하위 카테고리 ID들을 추가하는 헬퍼 메서드
    private void addChildCategoryIds(Long parentId, List<Category> allCategories, List<Long> result) {
        List<Category> children = allCategories.stream()
                .filter(c -> Optional.ofNullable(c.getParentId()).orElse(0L).equals(parentId))
                .toList();
        
        for (Category child : children) {
            result.add(child.getCategoryId());
            // 재귀 호출로 하위 카테고리들도 추가
            addChildCategoryIds(child.getCategoryId(), allCategories, result);
        }
    }



}
