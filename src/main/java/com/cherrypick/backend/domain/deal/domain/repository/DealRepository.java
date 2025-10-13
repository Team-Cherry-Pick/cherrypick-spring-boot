package com.cherrypick.backend.domain.deal.domain.repository;

import com.cherrypick.backend.domain.deal.domain.entity.Deal;
import com.cherrypick.backend.domain.deal.domain.enums.PriceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long>, DealRepositoryCustom {
    @Query(value = "SELECT b.* " +
            "FROM deal b " +
            "         INNER JOIN deal_tag hb ON b.deal_id = hb.deal_id " +
            "         INNER JOIN hash_tag h ON hb.hash_tag_id = h.hash_tag_id " +
            "WHERE h.hash_tag_id IN :tagIds " +
            "ORDER BY RAND() LIMIT :count;", nativeQuery = true)
    List<Deal> findDealsByTagId(@Param("tagIds")List<Long> tagIds, @Param("count") int count);

    @Modifying
    @Query("UPDATE Deal d SET d.totalViews = d.totalViews + 1 WHERE d.dealId = :dealId")
    int incrementViewCount(@Param("dealId") Long dealId);

    @Modifying
    @Query("UPDATE Deal d SET d.heat = d.heat + :amount WHERE d.dealId = :dealId")
    int updateHeat(@Param("dealId") Long dealId, @Param("amount") Double amount);

}