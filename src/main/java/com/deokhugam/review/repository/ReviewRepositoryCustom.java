package com.deokhugam.review.repository;

import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.entity.Review;
import java.util.List;

public interface ReviewRepositoryCustom {

    List<Review> findAllByCursor(ReviewSearchRequest request);

    long countAll(ReviewSearchRequest request);
}