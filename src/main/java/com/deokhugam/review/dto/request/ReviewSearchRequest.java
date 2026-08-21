package com.deokhugam.review.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
                regexp = "ASC|DESC",
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

    @AssertTrue(message = "커서 형식이 올바르지 않습니다.")
    public boolean isCursorValid() {
        if (cursor == null || cursor.isBlank()) {
            return true;
        }

        try {
            if ("rating".equals(orderBy)) {
                Integer.parseInt(cursor);
                return true;
            }

            if ("createdAt".equals(orderBy)) {
                LocalDateTime.parse(cursor);
                return true;
            }

            return true;
        } catch (
                NumberFormatException
                | DateTimeParseException exception
        ) {
            return false;
        }
    }

    @AssertTrue(
            message = "평점 정렬에서 cursor를 사용하는 경우 after가 필요합니다."
    )
    public boolean isRatingCursorPairValid() {
        if (!"rating".equals(orderBy)) {
            return true;
        }

        if (cursor == null || cursor.isBlank()) {
            return true;
        }

        return after != null;
    }
}
