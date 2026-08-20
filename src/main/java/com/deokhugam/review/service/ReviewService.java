package com.deokhugam.review.service;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import java.util.UUID;

public interface ReviewService {

    ReviewDetailResponse create(ReviewCreateRequest request);

    ReviewDetailResponse findById(
            UUID reviewId,
            UUID requesterId
    );

    ReviewDetailResponse update(
            UUID reviewId,
            UUID requesterId,
            ReviewUpdateRequest request
    );

    void softDelete(
            UUID reviewId,
            UUID requesterId
    );
}
