package com.cherrypick.backend.domain.hashtag.repository;

import com.cherrypick.backend.domain.hashtag.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    @Query(value="SELECT * FROM hash_tag WHERE name=:name;", nativeQuery=true)
    public Optional<HashTag> findByName(String name);

    @Query(value = "Select hash_tag_id from hash_tag where name like %:name% LIMIT 20;", nativeQuery = true)
    List<Integer> findAllByName(@Param("name") String name);

    @Query(value = "SELECT h.name FROM deal_tag dt INNER JOIN hash_tag h on dt.hash_tag_id = h.hash_tag_id WHERE dt.deal_id=:dealId", nativeQuery = true)
    List<String> findAllByDealId(@Param("dealId") Long dealId);
}
