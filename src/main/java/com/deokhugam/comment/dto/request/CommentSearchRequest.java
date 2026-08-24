package com.deokhugam.comment.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record CommentSearchRequest(

        @NotNull
        UUID reviewId,

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

    public CommentSearchRequest {
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
            UUID.fromString(cursor);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    @AssertTrue(
            message = "cursor를 사용하는 경우 after가 필요합니다."
    )
    public boolean isCursorAfterPairValid() {
        if (cursor == null || cursor.isBlank()) {
            return true;
        }

        return after != null;
    }
}