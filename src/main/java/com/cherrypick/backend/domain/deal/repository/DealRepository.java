package com.cherrypick.backend.domain.deal.repository;

import com.cherrypick.backend.domain.deal.entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {
}
