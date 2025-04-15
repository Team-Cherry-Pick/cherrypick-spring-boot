package com.cherrypick.backend.domain.vote.entity;

import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.domain.vote.enums.VoteType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voteId;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private Deal dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

}
