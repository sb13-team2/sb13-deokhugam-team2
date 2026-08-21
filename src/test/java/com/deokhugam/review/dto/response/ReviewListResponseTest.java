package com.deokhugam.review.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewListResponseTest {

    @Test
    void 리뷰_목록_응답을_생성한다() {
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReviewListItemResponse item =
                new ReviewListItemResponse(
                        reviewId,
                        bookId,
                        "테스트 도서",
                        "https://example.com/thumbnail.jpg",
                        userId,
                        "리아",
                        "좋은 책입니다.",
                        5,
                        3,
                        2,
                        true,
                        now,
                        now
                );

        ReviewListResponse response =
                new ReviewListResponse(
                        List.of(item),
                        "5",
                        now,
                        1,
                        1L,
                        false
                );

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id())
                .isEqualTo(reviewId);
        assertThat(response.content().get(0).likedByMe())
                .isTrue();
        assertThat(response.nextCursor()).isEqualTo("5");
        assertThat(response.nextAfter()).isEqualTo(now);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.totalElements()).isEqualTo(1L);
        assertThat(response.hasNext()).isFalse();
    }
}