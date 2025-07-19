package com.cherrypick.backend.domain.hashtag.repository;

import com.cherrypick.backend.domain.hashtag.entity.DealTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealTagRepository extends JpaRepository<DealTag, Long>
{

}
