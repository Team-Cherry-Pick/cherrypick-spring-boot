package com.cherrypick.backend.domain.deal.dto.response;

import com.cherrypick.backend.domain.deal.vo.Price;
import com.cherrypick.backend.domain.deal.vo.Shipping;
import com.cherrypick.backend.domain.image.vo.ImageUrl;
import com.cherrypick.backend.domain.user.vo.User;
import com.cherrypick.backend.domain.store.vo.Store;

import java.util.List;

public record DealDetailResponseDTO (

        Long dealId,
        List<ImageUrl> imageUrls,
        User user,
        Store store,
        List<String> categorys,
        String title,
        List<String> infoTags,
        Shipping shipping,
        Price price,
        String content,
        int heat,
        int totalViews,
        int totalLikes,
        int totalUnLikes,
        int totalComments,
        String deepLink,
        String originalUrl,
        boolean isSoldOut
) {
}
