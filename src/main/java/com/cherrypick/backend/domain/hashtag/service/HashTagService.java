package com.cherrypick.backend.domain.hashtag.service;

import com.cherrypick.backend.domain.hashtag.entity.DealTag;
import com.cherrypick.backend.domain.hashtag.entity.HashTag;
import com.cherrypick.backend.domain.hashtag.repository.DealTagRepository;
import com.cherrypick.backend.domain.hashtag.repository.HashTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service @RequiredArgsConstructor @Slf4j
public class HashTagService
{
    private final HashTagRepository hashTagRepository;
    private final DealTagRepository dealTagRepository;

    public void saveHashTags(Long targetDealId, Set<String> tagSet)
    {
        var dealTagList = new ArrayList<DealTag>();
        log.info("::::: 새로운 글 감지");
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
        log.info("해시태그 연결 : {}", dealTagList);

        dealTagRepository.saveAll(dealTagList);

    }

}
