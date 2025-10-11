package com.cherrypick.backend.domain.deal.domain.service.reference;

import com.cherrypick.backend.domain.deal.application.dto.response.DealDetailResponseDTO;
import com.cherrypick.backend.domain.deal.application.dto.response.StoreResponseListDTO;
import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.repository.reference.StoreRepository;
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
            var list = storeRepository.findAllVisible();
            response = new StoreResponseListDTO(
                    list.stream()
                            .map(StoreResponseListDTO.StoreResponseDTO::from)
                            .toList());
            redisTemplate.opsForValue().set("cache:stores", response, 1, TimeUnit.DAYS);
        }

        return (StoreResponseListDTO) response;
    }

    /**
     * Deal의 스토어 정보를 StoreVO로 변환
     * - Store 엔티티가 있으면 전체 정보 반환
     * - 없으면 storeName만 반환
     *
     * @param deal 딜 엔티티
     * @return 스토어 VO
     */
    public DealDetailResponseDTO.StoreVO getStoreInfo(Deal deal) {
        if (deal.getStore() != null) {
            // Store 엔티티가 있는 경우
            return new DealDetailResponseDTO.StoreVO(
                    deal.getStore().getStoreId(),
                    deal.getStore().getName(),
                    deal.getStore().getTextColor(),
                    deal.getStore().getBackgroundColor()
            );
        } else {
            // storeName만 있는 경우
            return new DealDetailResponseDTO.StoreVO(
                    null,
                    deal.getStoreName(),
                    null,
                    null
            );
        }
    }

}
