package com.cherrypick.backend.domain.deal.dto.response;

import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.deal.vo.Shipping;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.user.vo.UserVO;
import com.cherrypick.backend.domain.store.vo.Store;
import com.cherrypick.backend.domain.vote.enums.VoteType;

import java.util.List;

public record DealDetailResponseDTO (

        Long dealId,
        List<ImageUrl> imageUrls,
        UserVO user,
        Store store,
        List<String> categorys,
        String title,
        List<String> infoTags,
        Shipping shipping,
        Price price,
        String content,
        String discountDescription,
        int heat,
        int totalViews,
        int totalLikes,
        int totalUnLikes,
        int totalComments,
        String originalUrl,
        String deepLink,
        boolean isSoldOut,
        VoteType voteType,
        Long categoryId,
        Long storeId,
        List<Long> discountIds,
        String discountName

) {
}
