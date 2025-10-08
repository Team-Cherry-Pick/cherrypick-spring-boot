package com.cherrypick.backend.domain.deal.domain.service.reference;

import com.cherrypick.backend.domain.deal.application.dto.response.DiscountResponseListDTO;
import com.cherrypick.backend.domain.deal.domain.repository.reference.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor @Log4j2
public class DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountResponseListDTO getDiscountList() {

        var list = discountRepository.findAll();
        return new DiscountResponseListDTO(
                list.stream().map(DiscountResponseListDTO::from)
                .toList()
        );
    }


}
