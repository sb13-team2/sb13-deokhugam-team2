package com.deokhugam.review.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

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