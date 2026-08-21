package com.deokhugam.review.dto.response;

import java.util.UUID;

public record ReviewLikeResponse(
        UUID reviewId,
        UUID userId,
        boolean liked
) {
}