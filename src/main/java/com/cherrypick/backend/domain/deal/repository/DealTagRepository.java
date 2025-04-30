package com.cherrypick.backend.domain.deal.repository;

import com.cherrypick.backend.domain.deal.entity.DealTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealTagRepository extends JpaRepository<DealTag, Long>
{

}
