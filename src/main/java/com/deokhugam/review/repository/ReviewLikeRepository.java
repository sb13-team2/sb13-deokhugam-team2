package com.deokhugam.review.repository;

import com.deokhugam.review.entity.ReviewLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLikeRepository
        extends JpaRepository<ReviewLike, UUID> {

    boolean existsByReviewIdAndUserId(
            UUID reviewId,
            UUID userId
    );

    Optional<ReviewLike> findByReviewIdAndUserId(
            UUID reviewId,
            UUID userId
    );
}