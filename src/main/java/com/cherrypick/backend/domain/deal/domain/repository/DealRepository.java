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

public interface DealRepository extends JpaRepository<Deal, Long> {

    @Query("""
SELECT d FROM Deal d
JOIN FETCH d.category c
LEFT JOIN FETCH d.discounts discount
LEFT JOIN FETCH d.store store
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
    int incrementViewCount(@Param("dealId") Long dealId);

    @Modifying
    @Query("UPDATE Deal d SET d.heat = d.heat + :amount WHERE d.dealId = :dealId")
    int updateHeat(@Param("dealId") Long dealId, @Param("amount") Double amount);

    @Query(value="SELECT * FROM deal where user_id=:userId", nativeQuery = true)
    List<Deal> findDealsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자가 좋아요(추천)를 누른 모든 딜을 조회합니다.
     *
     * @param userId 조회할 사용자의 ID
     * @return 해당 사용자가 좋아요를 누른 딜 목록 (빈 리스트 가능)
     */
    @Query(value = "SELECT DISTINCT d.* FROM deal d " +
            "INNER JOIN vote v ON d.deal_id = v.deal_id " +
            "WHERE v.user_id = :userId AND v.vote_type = 'TRUE'",
            nativeQuery = true)
    List<Deal> findLikedDealsByUserId(@Param("userId") Long userId);

}