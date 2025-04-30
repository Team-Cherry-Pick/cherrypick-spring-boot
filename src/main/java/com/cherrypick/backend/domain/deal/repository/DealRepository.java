package com.cherrypick.backend.domain.deal.repository;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.enums.PriceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("""
    SELECT d FROM Deal d
    JOIN d.categoryId c
    LEFT JOIN d.discounts discount
    LEFT JOIN d.storeId store
    WHERE 
        d.isDelete = FALSE AND 
        (:categoryId IS NULL OR c.categoryId = :categoryId OR c.parentId = :categoryId)
        AND (:keyword IS NULL OR 
             LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR 
             LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:viewSoldOut = TRUE OR d.isSoldOut = FALSE)
        AND (:freeShipping = FALSE OR d.shipping.shippingType = 'FREE')
        AND (:startDate IS NULL OR d.createdAt >= :startDate)
        AND (:endDate IS NULL OR d.createdAt <= :endDate)
        AND (
            (
                (:priceType IS NULL OR d.price.priceType = :priceType)
                AND (:minPrice IS NULL OR d.price.discountedPrice >= :minPrice)
                AND (:maxPrice IS NULL OR d.price.discountedPrice <= :maxPrice)
            )
            OR (
                :variousPrice = TRUE AND d.price.priceType = 'VARIOUS'
            )
        )
        AND (:discountIds IS NULL OR discount.discountId IN :discountIds)
        AND (:storeIds IS NULL OR store.storeId IN :storeIds)
    ORDER BY
        CASE WHEN :sortPriceHigh = TRUE THEN d.price.discountedPrice END DESC,
        CASE WHEN :sortPriceLow = TRUE THEN d.price.discountedPrice END ASC,
        d.createdAt DESC
""")
    List<Deal> searchDealsWithPaging(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("viewSoldOut") boolean viewSoldOut,
            @Param("freeShipping") boolean freeShipping,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("priceType") PriceType priceType,
            @Param("variousPrice") boolean variousPrice,
            @Param("discountIds") List<Long> discountIds,
            @Param("storeIds") List<Long> storeIds,
            @Param("sortPriceHigh") boolean sortPriceHigh,
            @Param("sortPriceLow") boolean sortPriceLow,
            Pageable pageable
    );


    @Query(value = "SELECT b.* " +
            "FROM deal b " +
            "         INNER JOIN repik.deal_tag hb ON b.deal_id = hb.deal_id " +
            "         INNER JOIN hash_tag h ON hb.hash_tag_id = h.hash_tag_id " +
            "WHERE h.hash_tag_id IN :tagIds " +
            "ORDER BY RAND() LIMIT :count;", nativeQuery = true)
    List<Deal> findDealsByTagId(@Param("tagIds")List<Long> tagIds, @Param("count") int count);

}

