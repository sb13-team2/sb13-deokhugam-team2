package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.ReviewRankingAggregationRepository.ReviewAggregation;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewRankingBatchServiceTest {

  @Mock
  private DashboardPeriodResolver periodResolver;

  @Mock
  private ReviewRankingAggregationRepository aggregationRepository;

  @Mock
  private ReviewRankingRepository reviewRankingRepository;

  @InjectMocks
  private ReviewRankingBatchService reviewRankingBatchService;

  @Captor
  private ArgumentCaptor<List<ReviewRanking>> rankingsCaptor;

  @Test
  @DisplayName("인기 리뷰 점수 순으로 랭킹을 생성한다")
  void generateReviewRanking() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 24, 0, 0),
        LocalDateTime.of(2026, 8, 25, 0, 0)
    );

    UUID firstReviewId =
        UUID.fromString("00000000-0000-0000-0000-000000000001");

    UUID secondReviewId =
        UUID.fromString("00000000-0000-0000-0000-000000000002");

    UUID thirdReviewId =
        UUID.fromString("00000000-0000-0000-0000-000000000003");

    when(periodResolver.resolve(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of(
            new ReviewAggregation(
                secondReviewId,
                20L,
                5L
            ),
            new ReviewAggregation(
                thirdReviewId,
                10L,
                5L
            ),
            new ReviewAggregation(
                firstReviewId,
                10L,
                10L
            )
        ));

    reviewRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    InOrder inOrder = inOrder(reviewRankingRepository);

    inOrder.verify(reviewRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    inOrder.verify(reviewRankingRepository)
        .saveAll(rankingsCaptor.capture());

    List<ReviewRanking> rankings =
        rankingsCaptor.getValue();

    assertThat(rankings).hasSize(3);

    assertThat(rankings)
        .extracting(ReviewRanking::getReviewId)
        .containsExactly(
            firstReviewId,
            secondReviewId,
            thirdReviewId
        );

    assertThat(rankings)
        .extracting(ReviewRanking::getRanking)
        .containsExactly(
            1L,
            2L,
            3L
        );

    assertThat(rankings)
        .extracting(ReviewRanking::getScore)
        .containsExactly(
            10.0,
            9.5,
            6.5
        );

    assertThat(rankings.get(0).getPeriodType())
        .isEqualTo(PeriodType.DAILY);

    assertThat(rankings.get(0).getBaseDate())
        .isEqualTo(baseDate);

    assertThat(rankings.get(0).getLikeCount())
        .isEqualTo(10L);

    assertThat(rankings.get(0).getCommentCount())
        .isEqualTo(10L);
  }

  @Test
  @DisplayName("집계 결과가 없어도 기존 리뷰 랭킹 스냅샷을 삭제한다")
  void clearSnapshotWhenNoRankingExists() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 24, 0, 0),
        LocalDateTime.of(2026, 8, 25, 0, 0)
    );

    when(periodResolver.resolve(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of());

    reviewRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    verify(reviewRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    verify(reviewRankingRepository)
        .saveAll(List.of());
  }
}