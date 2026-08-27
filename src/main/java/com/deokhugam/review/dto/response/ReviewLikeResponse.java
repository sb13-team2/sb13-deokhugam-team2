package com.deokhugam.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "ReviewLikeDto")
public record ReviewLikeResponse(
        UUID reviewId,
        UUID userId,
        boolean liked
) {
}