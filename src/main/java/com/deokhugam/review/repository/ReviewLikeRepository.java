package com.deokhugam.review.repository;

import com.deokhugam.review.entity.ReviewLike;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
            """
            SELECT reviewLike.review.id
            FROM ReviewLike reviewLike
            WHERE reviewLike.user.id = :userId
              AND reviewLike.review.id IN :reviewIds
            """
    )
    Set<UUID> findLikedReviewIds(
            @Param("userId") UUID userId,
            @Param("reviewIds") Collection<UUID> reviewIds
    );
}