package com.cherrypick.backend.domain.store.repository;

import com.cherrypick.backend.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(value="SELECT * FROM store WHERE is_blind=false", nativeQuery = true)
    List<Store> findAllVisible();

}
