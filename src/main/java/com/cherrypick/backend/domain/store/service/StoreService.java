package com.cherrypick.backend.domain.store.service;

import com.cherrypick.backend.domain.store.dto.StoreResponseListDTO;
import com.cherrypick.backend.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service @Slf4j @RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreResponseListDTO getStoreList()
    {
        var list = storeRepository.findAll();
        return new StoreResponseListDTO(
                list.stream()
                .map(StoreResponseListDTO.StoreResponseDTO::from)
                .toList()
        );
    }

}
