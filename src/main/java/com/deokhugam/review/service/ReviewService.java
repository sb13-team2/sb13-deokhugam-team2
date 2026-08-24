package com.deokhugam.review.service;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;

import java.util.UUID;

public interface ReviewService {

    ReviewDetailResponse create(ReviewCreateRequest request);

    ReviewListResponse findAll(
            ReviewSearchRequest request,
            UUID requesterId
    );

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

    ReviewLikeResponse toggleLike(
            UUID reviewId,
            UUID requesterId
    );

    void hardDelete(
            UUID reviewId,
            UUID requesterId
    );
}
