package com.cherrypick.backend.domain.vote.dto.request;

import com.cherrypick.backend.domain.vote.enums.DislikeReason;
import com.cherrypick.backend.domain.vote.enums.VoteType;

public record VoteRequestDTO (

        VoteType voteType,
        DislikeReason dislikeReason
) {}