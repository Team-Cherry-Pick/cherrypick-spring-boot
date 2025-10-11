package com.cherrypick.backend.domain.deal.domain.repository.reference;

import com.cherrypick.backend.domain.deal.domain.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
