package com.cherrypick.backend.domain.deal.repository;

import com.cherrypick.backend.domain.category.entity.Category;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.enums.PriceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("""
SELECT d FROM Deal d
JOIN FETCH d.categoryId c
LEFT JOIN FETCH d.discounts discount
LEFT JOIN FETCH d.storeId store
WHERE 
    d.isDelete = FALSE
    AND (:categoryIds IS NULL OR c.categoryId in :categoryIds)
    AND (:keyword IS NULL OR 
         LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
         LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:viewSoldOut = TRUE OR d.isSoldOut = FALSE)
    AND (:freeShipping = FALSE OR d.shipping.shippingType = 'FREE')
    AND (:globalShipping = FALSE OR d.price.priceType = 'USD')
    AND (:startDate IS NULL OR d.createdAt >= :startDate)
    AND (:endDate IS NULL OR d.createdAt <= :endDate)
    AND (
        (d.price.priceType = 'VARIOUS' AND :variousPrice = TRUE)
        OR (
            (:priceTypes IS NULL OR d.price.priceType IN :priceTypes)
            AND (:minPrice IS NULL OR d.price.discountedPrice >= :minPrice)
            AND (:maxPrice IS NULL OR d.price.discountedPrice <= :maxPrice)
        )
    )
    AND (:discountIds IS NULL OR discount.discountId IN :discountIds)
    AND (:storeIds IS NULL OR store.storeId IN :storeIds)
""")
    List<Deal> searchDealsWithPaging(
            @Param("categoryIds") List<Long> categoryIds,
            @Param("keyword") String keyword,
            @Param("viewSoldOut") boolean viewSoldOut,
            @Param("freeShipping") boolean freeShipping,
            @Param("globalShipping") boolean globalShipping,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("priceTypes") List<PriceType> priceTypes,
            @Param("variousPrice") boolean variousPrice,
            @Param("discountIds") List<Long> discountIds,
            @Param("storeIds") List<Long> storeIds,
            Pageable pageable
    );

    @Query(value = "SELECT b.* " +
            "FROM deal b " +
            "         INNER JOIN deal_tag hb ON b.deal_id = hb.deal_id " +
            "         INNER JOIN hash_tag h ON hb.hash_tag_id = h.hash_tag_id " +
            "WHERE h.hash_tag_id IN :tagIds " +
            "ORDER BY RAND() LIMIT :count;", nativeQuery = true)
    List<Deal> findDealsByTagId(@Param("tagIds")List<Long> tagIds, @Param("count") int count);

    @Modifying
    @Query("UPDATE Deal d SET d.totalViews = d.totalViews + 1 WHERE d.dealId = :dealId")
    Optional<Integer> incrementViewCount(@Param("dealId") Long dealId);

    @Modifying
    @Query("UPDATE Deal d SET d.heat = d.heat + :amount WHERE d.dealId = :dealId")
    int updateHeat(@Param("dealId") Long dealId, @Param("amount") Double amount);

}