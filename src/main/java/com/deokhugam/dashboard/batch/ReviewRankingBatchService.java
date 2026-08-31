package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.ReviewRankingAggregationRepository.ReviewAggregation;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.event.TopReviewRankedEvent;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.util.DashboardScoreCalculator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewRankingBatchService {

  private final DashboardPeriodResolver periodResolver;
  private final ReviewRankingAggregationRepository aggregationRepository;
  private final ReviewRankingRepository reviewRankingRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void generateAll(LocalDate baseDate) {
    for (PeriodType periodType : PeriodType.values()) {
      generatePeriod(periodType, baseDate);
    }
  }

  @Transactional
  public void generate(
      PeriodType periodType,
      LocalDate baseDate
  ) {
    generatePeriod(periodType, baseDate);
  }

  private void generatePeriod(
      PeriodType periodType,
      LocalDate baseDate
  ) {
    PeriodRange periodRange =
        periodResolver.resolve(periodType, baseDate);

    List<ReviewAggregation> aggregations =
        aggregationRepository.aggregate(periodRange);

    List<ReviewCandidate> candidates = aggregations.stream()
        .map(aggregation -> new ReviewCandidate(
            aggregation,
            DashboardScoreCalculator.calculateReviewScore(
                aggregation.likeCount(),
                aggregation.commentCount()
            )
        ))
        .sorted(
            Comparator
                .comparingDouble(ReviewCandidate::score)
                .reversed()
                .thenComparing(
                    candidate -> candidate.aggregation().reviewId()
                )
        )
        .toList();

    List<ReviewRanking> rankings = new ArrayList<>();

    long rank = 1L;

    for (ReviewCandidate candidate : candidates) {
      ReviewAggregation aggregation =
          candidate.aggregation();

      rankings.add(
          ReviewRanking.builder()
              .reviewId(aggregation.reviewId())
              .periodType(periodType)
              .ranking(rank)
              .score(candidate.score())
              .baseDate(baseDate)
              .likeCount(aggregation.likeCount())
              .commentCount(aggregation.commentCount())
              .build()
      );

      rank++;
    }

    reviewRankingRepository.deleteSnapshot(
        periodType,
        baseDate
    );

    reviewRankingRepository.saveAll(rankings);

    List<UUID> top10ReviewIds = rankings.stream()
        .filter(ranking -> ranking.getRanking() <= 10)
        .map(ReviewRanking::getReviewId)
        .toList();

    if (!top10ReviewIds.isEmpty()) {
      eventPublisher.publishEvent(
          new TopReviewRankedEvent(top10ReviewIds, periodType)
      );
    }
  }

  private record ReviewCandidate(
      ReviewAggregation aggregation,
      double score
  ) {
  }
}