package com.cherrypick.backend.domain.store.dto;

import com.cherrypick.backend.domain.store.entity.Store;
import lombok.Builder;

import java.util.List;

@Builder
public record StoreResponseListDTO(
        List<StoreResponseDTO> stores
)
{
    @Builder
    public record StoreResponseDTO(
            Long storeId,
            String name,
            boolean isAffiliate,
            String backgorundColor,
            String textColoer,
            String storeRank
    ){
        public static StoreResponseDTO from(Store store)
        {
            return StoreResponseDTO.builder()
                    .storeId(store.getStoreId())
                    .name(store.getName())
                    .isAffiliate(store.isAffiliate())
                    .backgorundColor(store.getBackgroundColor())
                    .textColoer(store.getTextColor())
                    .storeRank(store.getStoreRank())
                    .build();
        }

    }



}
