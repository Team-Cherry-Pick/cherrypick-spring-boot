package com.cherrypick.backend.domain.deal.dto.response;

import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealSearchResponseDTO {

    private Long dealId;
    private ImageUrl imageUrl;
    private String title;
    private String store;
    private List<String> infoTags;
    private Price price;
    private String createdAt;
    private int totalLikes;
    private int totalComments;
    private boolean isSoldout;



}
