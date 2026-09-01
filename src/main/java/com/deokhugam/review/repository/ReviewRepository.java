package com.deokhugam.review.repository;

import com.deokhugam.review.entity.Review;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository
        extends JpaRepository<Review, UUID>,
        ReviewRepositoryCustom {

    boolean existsByUserIdAndBookIdAndDeletedAtIsNull(
            UUID userId,
            UUID bookId
    );

    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT review
        FROM Review review
        WHERE review.id = :reviewId
          AND review.deletedAt IS NULL
        """)
    Optional<Review> findByIdForUpdate(
            @Param("reviewId") UUID reviewId
    );

    List<Review> findAllByIdInAndDeletedAtIsNull(Collection<UUID> ids);

    // 도서 물리 삭제 시 연관 리뷰 조회
    List<Review> findAllByBookId(UUID bookId);
    // 유저가 작성한 리뷰 전체 조회를 위해 추가
    List<Review> findAllByUserId(UUID userId);
}