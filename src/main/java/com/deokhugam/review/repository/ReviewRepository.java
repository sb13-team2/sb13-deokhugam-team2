package com.deokhugam.review.repository;

import com.deokhugam.review.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByUserIdAndBookIdAndDeletedAtIsNull(
            UUID userId,
            UUID bookId
    );

    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);
}