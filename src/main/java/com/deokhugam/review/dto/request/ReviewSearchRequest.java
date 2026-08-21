package com.deokhugam.review.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewSearchRequest(

        UUID userId,

        UUID bookId,

        String keyword,

        @Pattern(
                regexp = "createdAt|rating",
                message = "정렬 기준은 createdAt 또는 rating이어야 합니다."
        )
        String orderBy,

        @Pattern(
                regexp = "(?i)ASC|DESC",
                message = "정렬 방향은 ASC 또는 DESC여야 합니다."
        )
        String direction,

        String cursor,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime after,

        @Min(
                value = 1,
                message = "페이지 크기는 1 이상이어야 합니다."
        )
        Integer limit

) {

    public ReviewSearchRequest {
        if (orderBy == null || orderBy.isBlank()) {
            orderBy = "createdAt";
        }

        if (direction == null || direction.isBlank()) {
            direction = "DESC";
        }

        if (limit == null) {
            limit = 50;
        }
    }
}
