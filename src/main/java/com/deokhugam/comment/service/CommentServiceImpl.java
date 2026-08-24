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
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse create(
            CommentCreateRequest request
    ) {
        Comment comment = new Comment(
                request.content(),
                request.userId(),
                request.reviewId()
        );

        Comment savedComment =
                commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse update(
            UUID commentId,
            UUID requesterId,
            CommentUpdateRequest request
    ) {
        Comment comment = findComment(commentId);

        validateOwner(comment, requesterId);

        if (comment.isDeleted()) {
            throw new DeokhugamException(
                    ErrorCode.COMMENT_ALREADY_DELETED
            );
        }

        comment.updateContent(request.content());

        return toResponse(comment);
    }

    @Override
    @Transactional
    public void delete(
            UUID commentId,
            UUID requesterId
    ) {
        Comment comment = findComment(commentId);

        validateOwner(comment, requesterId);

        if (!comment.isDeleted()) {
            comment.softDelete();
        }
    }

    @Override
    public CommentListResponse findAll(
            CommentSearchRequest request
    ) {
        List<Comment> searchedComments =
                commentRepository.findAllByCursor(request);

        boolean hasNext =
                searchedComments.size() > request.limit();

        List<Comment> comments = hasNext
                ? searchedComments.subList(
                0,
                request.limit()
        )
                : searchedComments;

        List<CommentResponse> content =
                comments.stream()
                        .map(this::toResponse)
                        .toList();

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext && !comments.isEmpty()) {
            Comment lastComment =
                    comments.get(comments.size() - 1);

            /*
             * cursor = 마지막 댓글 ID
             * after  = 마지막 댓글 생성 시간
             */
            nextCursor =
                    lastComment.getId().toString();

            nextAfter =
                    lastComment.getCreatedAt();
        }

        long totalElements =
                commentRepository.countAll(request);

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
                        () -> new DeokhugamException(
                                ErrorCode.COMMENT_NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            Comment comment,
            UUID requesterId
    ) {
        if (!comment.getUserId().equals(requesterId)) {
            throw new DeokhugamException(
                    ErrorCode.COMMENT_ACCESS_DENIED
            );
        }
    }

    private CommentResponse toResponse(
            Comment comment
    ) {
        String userNickname =
                userRepository
                        .findById(comment.getUserId())
                        .map(User::getNickname)
                        .orElse("");

        return CommentResponse.from(
                comment,
                userNickname
        );
    }
}