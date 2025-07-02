package com.cherrypick.backend.domain.comment.service;

import com.cherrypick.backend.domain.comment.dto.request.CommentRequestDTOs;
import com.cherrypick.backend.domain.comment.dto.response.BestCommentResponseDTO;
import com.cherrypick.backend.domain.comment.dto.response.CommentListResponseDTO;
import com.cherrypick.backend.domain.comment.dto.response.CommentResponseDTOs;
import com.cherrypick.backend.domain.comment.entity.Comment;
import com.cherrypick.backend.domain.comment.entity.CommentLike;
import com.cherrypick.backend.domain.comment.entity.CommentLikeId;
import com.cherrypick.backend.domain.comment.enums.SortType;
import com.cherrypick.backend.domain.comment.repository.CommentLikeRepository;
import com.cherrypick.backend.domain.comment.repository.CommentRepository;
import com.cherrypick.backend.domain.deal.entity.Deal;
import com.cherrypick.backend.domain.deal.repository.DealRepository;
import com.cherrypick.backend.domain.image.entity.Image;
import com.cherrypick.backend.domain.image.repository.ImageRepository;
import com.cherrypick.backend.domain.user.dto.AuthenticationDetailDTO;
import com.cherrypick.backend.domain.user.repository.UserRepository;
import com.cherrypick.backend.domain.user.entity.User;
import com.cherrypick.backend.global.exception.BaseException;
import com.cherrypick.backend.global.exception.enums.CommentErrorCode;
import com.cherrypick.backend.global.exception.enums.DealErrorCode;
import com.cherrypick.backend.global.exception.enums.GlobalErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ImageRepository imageRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDTOs.Create createComment(Long dealId, CommentRequestDTOs.Create request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Deal deal = dealRepository.findById(dealId)
                .orElseThrow(() -> new BaseException(DealErrorCode.DEAL_NOT_FOUND));

        // 부모 댓글 없는 경우 (parentId가 왔을 때)
        if (request.parentId() != null) {
            Comment parentComment = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BaseException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND));

            // 대댓글에 대댓글을 단 경우
            if (parentComment.getParentId() != null) {
                throw new BaseException(CommentErrorCode.CANNOT_REPLY_TO_REPLY);
            }
        }

        // 댓글 내용 없는 경우
        if (request.content() == null || request.content().isBlank()) {
            throw new BaseException(CommentErrorCode.MISSING_COMMENT_CONTENT);
        }

        Comment comment = new Comment();
        comment.setDealId(deal);
        comment.setParentId(request.parentId());
        comment.setUserId(user);
        comment.setContent(request.content());
        comment.setDelete(false);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        return new CommentResponseDTOs.Create(comment.getCommentId(), "댓글 작성 성공");
    }

    // 댓글 전체 조회
    @Transactional
    public List<CommentListResponseDTO> getCommentList(Long dealId, SortType sortType) {
        boolean exists = dealRepository.existsById(dealId);
        if (!exists) {
            throw new BaseException(DealErrorCode.DEAL_NOT_FOUND);
        }

        // 댓글이 없는 경우
        List<Comment> allComments = commentRepository.findAllByDealId(dealId);
        if (allComments.isEmpty()) {
            throw new BaseException(CommentErrorCode.NO_COMMENTS_FOUND);
        }

        // 부모 댓글 조회
        List<Comment> parentComments = switch (sortType) {
            case POPULAR -> commentRepository.findParentCommentsByLikes(dealId);
            case LATEST -> commentRepository.findParentCommentsLatest(dealId);
        };

        return parentComments.stream()
                .map(parent -> {
                    // 대댓글 조회 (작성된 순으로)
                    List<Comment> replies = commentRepository.findReplies(parent.getCommentId());

                    List<CommentListResponseDTO> replyDtos = replies.stream()
                            .map(this::toCommentDtoWithoutReplies)
                            .toList();

                    return toCommentDtoWithReplies(parent, replyDtos);
                })
                .toList();
    }

    // 베스트 댓글 조회
    @Transactional
    public List<BestCommentResponseDTO> getBestComments(Long dealId) {
        boolean exists = dealRepository.existsById(dealId);
        if (!exists) {
            throw new BaseException(DealErrorCode.DEAL_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findAllByDealIdAndIsDeleteFalse(dealId);

        // 댓글이 없는 경우
        if (comments.isEmpty()) {
            throw new BaseException(CommentErrorCode.NO_COMMENTS_FOUND);
        }

        List<BestCommentResponseDTO> result = comments.stream()
                .map(comment -> {
                    int totalLikes = commentLikeRepository.countByCommentId(comment);

                    // 프로필 이미지 조회
                    Optional<Image> imageOpt = imageRepository.findByUserId(
                            comment.getUserId().getUserId()
                    );

                    return new BestCommentResponseDTO(
                            comment.getCommentId(),
                            new com.cherrypick.backend.domain.user.vo.User(
                                    comment.getUserId().getUserId(),
                                    comment.getUserId().getNickname(),
                                    imageOpt.map(Image::getImageUrl).orElse(null)
                            ),
                            totalLikes,
                            comment.getContent()
                    );
                })
                .sorted(
                        Comparator.comparingInt(BestCommentResponseDTO::totalLikes).reversed()
                                .thenComparing(BestCommentResponseDTO::commentId, Comparator.reverseOrder())
                )
                .limit(2)
                .toList();

        return result;
    }

    // 댓글 삭제 (Soft Delete)
    @Transactional
    public CommentResponseDTOs.Delete deleteComment(Long commentId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_NOT_FOUND));

        // 작성자 검증
        if (!comment.getUserId().getUserId().equals(user.getUserId())) {
            throw new BaseException(GlobalErrorCode.FORBIDDEN);
        }

        comment.setDelete(true);

        return new CommentResponseDTOs.Delete(comment.getCommentId(), "댓글 삭제 성공");
    }

    // 댓글 좋아요
    @Transactional
    public CommentResponseDTOs.Like likeComment(CommentRequestDTOs.Like request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof AuthenticationDetailDTO userDetails)) {
            throw new BaseException(GlobalErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new BaseException(GlobalErrorCode.UNAUTHORIZED));

        Comment comment = commentRepository.findById(request.commentID())
                .orElseThrow(() -> new BaseException(CommentErrorCode.COMMENT_NOT_FOUND));

        CommentLikeId likeId = new CommentLikeId(user.getUserId(), comment.getCommentId());

        boolean exists = commentLikeRepository.existsById(likeId);

        String message;

        if (request.isLike()) {
            // 좋아요 추가
            if (!exists) {
                CommentLike like = new CommentLike(comment, user);
                commentLikeRepository.save(like);
            }
            message = "댓글 좋아요 성공";
        } else {
            // 좋아요 취소
            if (exists) {
                commentLikeRepository.deleteById(likeId);
            }
            message = "댓글 좋아요 취소 성공";
        }

        return new CommentResponseDTOs.Like(comment.getCommentId(), message);
    }

    // DTO 변환 메소드들

    // 부모 댓글 + 대댓글 리스트까지 포함한 DTO 만들기
    private CommentListResponseDTO toCommentDtoWithReplies(Comment comment, List<CommentListResponseDTO> replies) {
        int totalLikes = commentLikeRepository.countByCommentId(comment);
        int totalReplys = replies.size();

        Optional<Image> imageOpt = imageRepository.findByUserId(
                comment.getUserId().getUserId()
        );

        return new CommentListResponseDTO(
                comment.getCommentId(),
                comment.getParentId(),
                new com.cherrypick.backend.domain.user.vo.User(
                        comment.getUserId().getUserId(),
                        comment.getUserId().getNickname(),
                        imageOpt.map(Image::getImageUrl).orElse(null)
                ),
                comment.getContent(),
                totalLikes,
                totalReplys,
                comment.getCreatedAt(),
                comment.isDelete(),
                replies
        );
    }

    // 대댓글 1개짜리 DTO 만들기
    private CommentListResponseDTO toCommentDtoWithoutReplies(Comment comment) {
        int totalLikes = commentLikeRepository.countByCommentId(comment);

        return new CommentListResponseDTO(
                comment.getCommentId(),
                comment.getParentId(),
                new com.cherrypick.backend.domain.user.vo.User(
                        comment.getUserId().getUserId(),
                        comment.getUserId().getNickname(),
                        null
                ),
                comment.getContent(),
                totalLikes,
                0,
                comment.getCreatedAt(),
                comment.isDelete(),
                List.of()
        );
    }

    public void dummyDataSetting(){
        var dummy_contents = List.of(
                "와 대박짱 맛있어보여요 완전 킹대박",
                "정말 어떻게 이런 가격이지? 정말 핫딜은 왕빵킹슈퍼대박이다.",
                "아~~~~~~~~~~~~~~~ 오늘은 아낀 돈으로 치킨 사먹어야지 다 죽었다 ㅋㅋㅋ",
                "헐 진짜 웃기는 짬뽕이야 ~ 이런 가격이면 누가 믿어요 !",
                "핫딜을 보면 웃는 개 : 왈왈왈왈왈왈왈왈왈왈왈왈!!!",
                "와 달다 정말 무등산수박만큼 달다.",
                "아 얼마 전에 샀었는데 ... 좀 늦게 살걸",
                "이거 또 올라왔어요? 저번보다 싸네",
                "와 좋은 정보 감사함당 ~",
                "ㅎㅎㅎㅎㅎ 오늘도 좋은 정보 고마워요",
                "아 지갑 얇아지는 소리 들린다 ㅠ 그래두 감사합니다",
                "이거 근데 왜 이렇게 싸요? 남는게 있나",
                "좋은데요? 근데 마진이 남나? 조금 의심스럽지 않아요?",
                "금방 나가겠네 얼른 사야겠다",
                "근데 이분 되게 자주 올리시네?ㅋㅋㅋ",
                "이거 정말 싸네요 !! 고수분들은 어떻게 생각하세요?",
                "오 카드 할인도 있네요 ~~ 다들 참고하세요",
                "아침부터 굳 핫딜이당 ~~ 굳굳",
                "나가기 전에 얼른 사세오!",
                "야호",
                "나도 사야겠다",
                "사고 싶은데 .. 흠...",
                "이번달도 적자네 ;",
                "앗 마침 필요했는데",
                "좋다좋아",
                "금방 나가겠는데 다들 언능 들여가세요",
                "와 ~~~ 울 마눌 사줘야겠어요 ~~~",
                "그이 몰래 들였네요 저 혼자 먹으려구요",
                "헉 아주 죠와 !!!!!!!!!",
                "이런 핫딜은 추천이야 진짜로"
        );

        var deals = dealRepository.findAll();
        var dealIds = deals.stream().map(Deal::getDealId).toList();

        var comments = new ArrayList<Comment>();
        for(var id : dealIds){
            int count = new Random().nextInt(20);
            for (int i = 0; i < count; i++) {
                var c = new Comment();
                c.setDealId(Deal.builder().dealId(id).build());
                c.setParentId(null);
                c.setUserId(User.builder().userId(1L).build());
                c.setContent(dummy_contents.get(new Random().nextInt(dummy_contents.size())));
                c.setDelete(false);
                comments.add(c);
            }
        }

        commentRepository.saveAll(comments);
    }


}
