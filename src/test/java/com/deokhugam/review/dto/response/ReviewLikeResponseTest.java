package com.deokhugam.review.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewLikeResponseTest {

    @Test
    void 리뷰_좋아요_응답을_생성한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewLikeResponse response = new ReviewLikeResponse(
                reviewId,
                userId,
                true
        );

        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.liked()).isTrue();
    }
}