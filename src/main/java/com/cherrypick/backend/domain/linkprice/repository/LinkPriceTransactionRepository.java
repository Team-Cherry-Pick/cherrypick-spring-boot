package com.cherrypick.backend.domain.linkprice.repository;

import com.cherrypick.backend.domain.linkprice.entity.LinkPriceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkPriceTransactionRepository extends JpaRepository<LinkPriceTransaction, String> {
}
