package com.deokhugam.comment.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CommentListResponse(
        List<CommentResponse> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}