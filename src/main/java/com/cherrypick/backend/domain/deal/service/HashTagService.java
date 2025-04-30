package com.cherrypick.backend.domain.deal.service;

import com.cherrypick.backend.domain.deal.entity.DealTag;
import com.cherrypick.backend.domain.deal.entity.HashTag;
import com.cherrypick.backend.domain.deal.repository.DealTagRepository;
import com.cherrypick.backend.domain.deal.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service @RequiredArgsConstructor @Slf4j
public class HashTagService
{
    private final HashTagRepository hashTagRepository;
    private final DealTagRepository dealTagRepository;

    public void saveHashTags(Long targetDealId, Set<String> tagSet)
    {
        var dealTagList = new ArrayList<DealTag>();
        for (String hashTag : tagSet) {

            var tagOp = hashTagRepository.findByName(hashTag);
            HashTag tag = null;
            tag = tagOp.orElseGet(() -> hashTagRepository.save(HashTag.builder()
                    .name(hashTag)
                    .build()));

            DealTag dealTag = DealTag.builder()
                    .dealId(targetDealId)
                    .hashTagId(tag.getHashTagId())
                    .build();

            dealTagList.add(dealTag);

        }

        dealTagRepository.saveAll(dealTagList);

    }

}
