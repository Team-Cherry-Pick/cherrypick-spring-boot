package com.cherrypick.backend.domain.store.repository;

import com.cherrypick.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}
