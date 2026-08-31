package com.deokhugam.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(name = "ReviewDto")
public record ReviewListItemResponse(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String content,
        Integer rating,
        Integer likeCount,
        Integer commentCount,
        Boolean likedByMe,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}