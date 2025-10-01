package com.cherrypick.backend.domain.discount.service;

import com.cherrypick.backend.domain.discount.dto.DiscountResponseListDTO;
import com.cherrypick.backend.domain.discount.repository.DiscountRepository;
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
