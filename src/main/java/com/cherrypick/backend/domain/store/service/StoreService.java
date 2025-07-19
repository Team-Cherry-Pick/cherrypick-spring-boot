package com.cherrypick.backend.domain.store.service;

import com.cherrypick.backend.domain.store.dto.StoreResponseListDTO;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service @Slf4j @RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public StoreResponseListDTO getStoreList()
    {

        var response = redisTemplate.opsForValue().get("cache:stores");
        if(response == null)
        {
            var list = storeRepository.findAll();
            response = new StoreResponseListDTO(
                    list.stream()
                            .map(StoreResponseListDTO.StoreResponseDTO::from)
                            .toList());
            redisTemplate.opsForValue().set("cache:stores", response, 1, TimeUnit.DAYS);
        }

        return (StoreResponseListDTO) response;
    }

}
