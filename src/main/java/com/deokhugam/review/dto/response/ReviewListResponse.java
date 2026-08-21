package com.deokhugam.review.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewListResponse(
        List<ReviewListItemResponse> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}