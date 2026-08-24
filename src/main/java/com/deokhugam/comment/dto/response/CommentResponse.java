package com.deokhugam.comment.dto.response;

import com.deokhugam.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        String userNickname,
        UUID reviewId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CommentResponse from(
            Comment comment,
            String userNickname
    ) {
        return new CommentResponse(
                comment.getId(),
                comment.getUserId(),
                userNickname,
                comment.getReviewId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}