package com.deokhugam.review.dto.request;

import com.deokhugam.review.dto.ReviewCursor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Schema(hidden = true)
    @AssertTrue(message = "커서 형식이 올바르지 않습니다.")
    public boolean isCursorValid() {
        if (cursor == null || cursor.isBlank()) {
            return true;
        }

        try {
            ReviewCursor reviewCursor = ReviewCursor.decode(cursor);

            if ("rating".equals(orderBy)) {
                Integer.parseInt(reviewCursor.sortValue());
                return true;
            }

            if ("createdAt".equals(orderBy)) {
                LocalDateTime.parse(reviewCursor.sortValue());
                return true;
            }

            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Schema(hidden = true)
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