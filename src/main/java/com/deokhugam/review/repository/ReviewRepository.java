package com.deokhugam.review.repository;

import com.deokhugam.review.entity.Review;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository
        extends JpaRepository<Review, UUID>,
        ReviewRepositoryCustom {

    boolean existsByUserIdAndBookIdAndDeletedAtIsNull(
            UUID userId,
            UUID bookId
    );

    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    List<Review> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}