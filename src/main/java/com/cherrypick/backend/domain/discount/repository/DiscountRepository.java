package com.cherrypick.backend.domain.discount.repository;

import com.cherrypick.backend.domain.discount.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
