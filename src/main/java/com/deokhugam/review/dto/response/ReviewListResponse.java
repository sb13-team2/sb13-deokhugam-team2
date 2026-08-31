package com.deokhugam.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(
        name = "CursorPageResponseReviewDto",
        description = "커서 기반 페이지 응답"
)
public record ReviewListResponse(

        @Schema(description = "페이지 내용")
        List<ReviewListItemResponse> content,

        @Schema(description = "다음 페이지 커서")
        String nextCursor,

        @Schema(
                description = "마지막 요소의 생성 시간",
                example = "2025-04-06T15:04:05.000Z"
        )
        LocalDateTime nextAfter,

        @Schema(
                description = "페이지 크기",
                example = "10"
        )
        int size,

        @Schema(
                description = "총 요소 수",
                example = "100"
        )
        long totalElements,

        @Schema(
                description = "다음 페이지 여부",
                example = "true"
        )
        boolean hasNext

) {
}