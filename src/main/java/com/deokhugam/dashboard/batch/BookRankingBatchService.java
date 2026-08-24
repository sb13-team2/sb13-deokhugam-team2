package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.BookRankingAggregationRepository.BookAggregation;
import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.entity.BookRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.repository.BookRankingRepository;
import com.deokhugam.dashboard.util.DashboardScoreCalculator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookRankingBatchService {

  private final DashboardPeriodResolver periodResolver;
  private final BookRankingAggregationRepository aggregationRepository;
  private final BookRankingRepository bookRankingRepository;

  @Transactional
  public void generateAll(LocalDate baseDate) {
    for (PeriodType periodType : PeriodType.values()) {
      generatePeriod(periodType, baseDate);
    }
  }

  @Transactional
  public void generate(PeriodType periodType, LocalDate baseDate) {
    generatePeriod(periodType, baseDate);
  }

  private void generatePeriod(
      PeriodType periodType,
      LocalDate baseDate
  ) {
    PeriodRange periodRange =
        periodResolver.resolve(periodType, baseDate);

    List<BookAggregation> aggregations =
        aggregationRepository.aggregate(periodRange);

    List<BookCandidate> candidates = aggregations.stream()
        .map(aggregation -> new BookCandidate(
            aggregation,
            DashboardScoreCalculator.calculateBookScore(
                aggregation.reviewCount(),
                aggregation.averageRating()
            )
        ))
        .sorted(
            Comparator
                .comparingDouble(BookCandidate::score)
                .reversed()
                .thenComparing(
                    candidate -> candidate.aggregation().bookId()
                )
        )
        .toList();

    List<BookRanking> rankings = new ArrayList<>();

    long rank = 1L;

    for (BookCandidate candidate : candidates) {
      BookAggregation aggregation = candidate.aggregation();

      rankings.add(
          BookRanking.builder()
              .bookId(aggregation.bookId())
              .periodType(periodType)
              .ranking(rank)
              .score(candidate.score())
              .baseDate(baseDate)
              .reviewCount(aggregation.reviewCount())
              .rating(aggregation.averageRating())
              .build()
      );

      rank++;
    }

    bookRankingRepository.deleteSnapshot(
        periodType,
        baseDate
    );

    bookRankingRepository.saveAll(rankings);
  }

  private record BookCandidate(
      BookAggregation aggregation,
      double score
  ) {
  }
}