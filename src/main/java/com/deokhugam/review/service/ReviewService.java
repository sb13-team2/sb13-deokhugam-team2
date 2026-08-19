package com.deokhugam.review.service;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;

public interface ReviewService {

    ReviewDetailResponse create(ReviewCreateRequest request);
}