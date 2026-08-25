package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRankingAggregationRepository {

  private final EntityManager entityManager;

  public List<ReviewAggregation> aggregate(PeriodRange periodRange) {
    TypedQuery<Object[]> reviewQuery;

    if (periodRange.startInclusive() == null) {
      reviewQuery = entityManager.createQuery(
          """
          SELECT r.id, r.likeCount
          FROM Review r
          WHERE r.createdAt < :endExclusive
            AND r.deletedAt IS NULL
          """,
          Object[].class
      );

      reviewQuery.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    } else {
      reviewQuery = entityManager.createQuery(
          """
          SELECT r.id, r.likeCount
          FROM Review r
          WHERE r.createdAt >= :startInclusive
            AND r.createdAt < :endExclusive
            AND r.deletedAt IS NULL
          """,
          Object[].class
      );

      reviewQuery.setParameter(
          "startInclusive",
          periodRange.startInclusive()
      );

      reviewQuery.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    }

    List<Object[]> reviewRows = reviewQuery.getResultList();

    if (reviewRows.isEmpty()) {
      return List.of();
    }

    List<UUID> reviewIds = reviewRows.stream()
        .map(row -> (UUID) row[0])
        .toList();

    TypedQuery<Object[]> commentQuery = entityManager.createQuery(
        """
        SELECT c.reviewId, COUNT(c)
        FROM Comment c
        WHERE c.reviewId IN :reviewIds
        GROUP BY c.reviewId
        """,
        Object[].class
    );

    commentQuery.setParameter("reviewIds", reviewIds);

    Map<UUID, Long> commentCountByReviewId =
        commentQuery.getResultList().stream()
            .collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).longValue()
            ));

    return reviewRows.stream()
        .map(row -> {
          UUID reviewId = (UUID) row[0];
          long likeCount = ((Number) row[1]).longValue();
          long commentCount =
              commentCountByReviewId.getOrDefault(reviewId, 0L);

          return new ReviewAggregation(
              reviewId,
              likeCount,
              commentCount
          );
        })
        .toList();
  }

  public record ReviewAggregation(
      UUID reviewId,
      long likeCount,
      long commentCount
  ) {
  }
}