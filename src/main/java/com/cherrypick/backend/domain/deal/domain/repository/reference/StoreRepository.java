package com.cherrypick.backend.domain.deal.domain.repository.reference;

import com.cherrypick.backend.domain.deal.domain.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    @Query(value="SELECT * FROM store WHERE is_blind=false", nativeQuery = true)
    List<Store> findAllVisible();

}
