package com.deokhugam.comment.service;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notification.entity.NotificationType;
import com.deokhugam.notification.service.NotificationService;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.exception.ReviewNotFoundException;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public CommentResponse create(
            CommentCreateRequest request
    ) {
        User commenter =
                findActiveUser(request.userId());

        Review review =
                findActiveReview(request.reviewId());

        Comment comment =
                new Comment(
                        request.content(),
                        commenter.getId(),
                        review.getId()
                );

        Comment savedComment =
                commentRepository.save(comment);

        commentRepository.increaseReviewCommentCount(
                review.getId()
        );

        createCommentNotification(
                review,
                commenter.getId()
        );

        return CommentResponse.from(
                savedComment,
                commenter.getNickname()
        );
    }

    @Override
    @Transactional
    public CommentResponse update(
            UUID commentId,
            UUID requesterId,
            CommentUpdateRequest request
    ) {
        Comment comment =
                findComment(commentId);

        validateOwner(
                comment,
                requesterId
        );

        if (comment.isDeleted()) {
            throw new DeokhugamException(
                    ErrorCode.COMMENT_ALREADY_DELETED
            );
        }

        comment.updateContent(
                request.content()
        );

        return toResponse(comment);
    }

    @Override
    @Transactional
    public void delete(
            UUID commentId,
            UUID requesterId
    ) {
        Comment comment =
                findComment(commentId);

        validateOwner(
                comment,
                requesterId
        );

        if (!comment.isDeleted()) {
            comment.softDelete();

            commentRepository.decreaseReviewCommentCount(
                    comment.getReviewId()
            );
        }
    }

    @Override
    @Transactional
    public void deleteAllByUserId(
            UUID userId
    ) {
        List<Comment> comments =
                commentRepository.findAllByUserId(userId);

        for (Comment comment : comments) {
            if (!comment.isDeleted()) {
                commentRepository.decreaseReviewCommentCount(
                        comment.getReviewId()
                );
            }
        }

        commentRepository.deleteAllByUserId(userId);
    }

    @Override
    public CommentListResponse findAll(
            CommentSearchRequest request
    ) {
        List<Comment> searchedComments =
                commentRepository.findAllByCursor(
                        request
                );

        boolean hasNext =
                searchedComments.size()
                        > request.limit();

        List<Comment> comments =
                hasNext
                        ? searchedComments.subList(
                        0,
                        request.limit()
                )
                        : searchedComments;

        Map<UUID, User> userMap =
                findUsersByComments(comments);

        List<CommentResponse> content =
                comments.stream()
                        .map(comment ->
                                toResponse(
                                        comment,
                                        userMap
                                )
                        )
                        .toList();

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext && !comments.isEmpty()) {
            Comment lastComment =
                    comments.get(
                            comments.size() - 1
                    );

            nextCursor =
                    lastComment
                            .getId()
                            .toString();

            nextAfter =
                    lastComment.getCreatedAt();
        }

        long totalElements =
                commentRepository.countAll(
                        request
                );

        return new CommentListResponse(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }

    private Comment findComment(
            UUID commentId
    ) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(
                        () ->
                                new DeokhugamException(
                                        ErrorCode.COMMENT_NOT_FOUND
                                )
                );
    }

    private User findActiveUser(
            UUID userId
    ) {
        return userRepository
                .findByIdAndDeletedAtIsNull(
                        userId
                )
                .orElseThrow(
                        () ->
                                new DeokhugamException(
                                        ErrorCode.USER_NOT_FOUND
                                )
                );
    }

    private Review findActiveReview(
            UUID reviewId
    ) {
        return reviewRepository
                .findByIdAndDeletedAtIsNull(
                        reviewId
                )
                .orElseThrow(
                        () ->
                                new ReviewNotFoundException(
                                        reviewId
                                )
                );
    }

    private void validateOwner(
            Comment comment,
            UUID requesterId
    ) {
        if (!comment
                .getUserId()
                .equals(requesterId)) {

            throw new DeokhugamException(
                    ErrorCode.COMMENT_ACCESS_DENIED
            );
        }
    }

    private void createCommentNotification(
            Review review,
            UUID commenterId
    ) {
        User reviewWriter =
                review.getUser();

        if (reviewWriter
                .getId()
                .equals(commenterId)) {
            return;
        }

        notificationService.createNotification(
                reviewWriter,
                review,
                "회원님의 리뷰에 새로운 댓글이 등록되었습니다.",
                NotificationType.NEW_COMMENT
        );
    }

    private Map<UUID, User> findUsersByComments(
            List<Comment> comments
    ) {
        if (comments.isEmpty()) {
            return Map.of();
        }

        Set<UUID> userIds =
                comments.stream()
                        .map(Comment::getUserId)
                        .collect(Collectors.toSet());

        /*
         * 논리 삭제된 사용자도 기존 댓글 작성자 정보는
         * 유지되어야 하므로 활성 사용자 전용 조회가 아닌
         * JpaRepository 기본 findAllById를 사용한다.
         */
        return userRepository
                .findAllById(userIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                User::getId,
                                Function.identity()
                        )
                );
    }

    private CommentResponse toResponse(
            Comment comment,
            Map<UUID, User> userMap
    ) {
        User user =
                userMap.get(
                        comment.getUserId()
                );

        String userNickname =
                user != null
                        ? user.getNickname()
                        : "";

        return CommentResponse.from(
                comment,
                userNickname
        );
    }

    private CommentResponse toResponse(
            Comment comment
    ) {
        String userNickname =
                userRepository
                        .findById(
                                comment.getUserId()
                        )
                        .map(User::getNickname)
                        .orElse("");

        return CommentResponse.from(
                comment,
                userNickname
        );
    }
}