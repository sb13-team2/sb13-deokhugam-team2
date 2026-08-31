package com.deokhugam.review.repository;

import com.deokhugam.review.entity.ReviewLike;

import java.util.*;

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

    void deleteAllByReviewId(UUID reviewId);

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

    // 유저가 누른 좋아요 전체 삭제를 위해 추가
    void deleteAllByUserId(UUID userId);
    // 유저가 누른 좋아요 전체 조회
    List<ReviewLike> findAllByUserId(UUID userId);
}