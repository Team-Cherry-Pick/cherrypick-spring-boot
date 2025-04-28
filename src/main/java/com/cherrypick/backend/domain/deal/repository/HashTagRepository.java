package com.cherrypick.backend.domain.deal.repository;

import com.cherrypick.backend.domain.deal.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashTagRepository extends JpaRepository<HashTag, Long> {

    @Query(value="SELECT * FROM hash_tag WHERE name=:name;", nativeQuery=true)
    public Optional<HashTag> findByName(String name);

}
