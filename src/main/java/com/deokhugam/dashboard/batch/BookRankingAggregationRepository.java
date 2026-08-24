package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRankingAggregationRepository {

  private final EntityManager entityManager;

  public List<BookAggregation> aggregate(PeriodRange periodRange) {
    TypedQuery<Object[]> query;

    if (periodRange.startInclusive() == null) {
      query = entityManager.createQuery(
          """
          SELECT r.book.id, COUNT(r), AVG(r.rating)
          FROM Review r
          WHERE r.createdAt < :endExclusive
          GROUP BY r.book.id
          """,
          Object[].class
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    } else {
      query = entityManager.createQuery(
          """
          SELECT r.book.id, COUNT(r), AVG(r.rating)
          FROM Review r
          WHERE r.createdAt >= :startInclusive
            AND r.createdAt < :endExclusive
          GROUP BY r.book.id
          """,
          Object[].class
      );

      query.setParameter(
          "startInclusive",
          periodRange.startInclusive()
      );
      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    }

    return query.getResultList()
        .stream()
        .map(row -> new BookAggregation(
            (UUID) row[0],
            ((Number) row[1]).longValue(),
            ((Number) row[2]).doubleValue()
        ))
        .toList();
  }

  public record BookAggregation(
      UUID bookId,
      long reviewCount,
      double averageRating
  ) {
  }
}