package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.ReviewRankingAggregationRepository.ReviewAggregation;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.event.TopReviewRankedEvent;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReviewRankingBatchServiceTest {

  @Mock
  private DashboardPeriodResolver periodResolver;

  @Mock
  private ReviewRankingAggregationRepository aggregationRepository;

  @Mock
  private ReviewRankingRepository reviewRankingRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

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

    ArgumentCaptor<TopReviewRankedEvent> eventCaptor =
        ArgumentCaptor.forClass(TopReviewRankedEvent.class);

    verify(eventPublisher)
        .publishEvent(eventCaptor.capture());

    TopReviewRankedEvent event = eventCaptor.getValue();

    assertThat(event.periodType())
        .isEqualTo(PeriodType.DAILY);

    assertThat(event.topReviewIds())
        .containsExactly(
            firstReviewId,
            secondReviewId,
            thirdReviewId
        );
  }

  @Test
  @DisplayName("집계 결과가 없으면 기존 리뷰 랭킹 스냅샷만 삭제한다")
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

    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("주간 인기 리뷰는 상위 10개만 이벤트로 발행한다")
  void publishWeeklyTopReviewEvent() {
    LocalDate baseDate = LocalDate.of(2026, 8, 30);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 24, 0, 0),
        LocalDateTime.of(2026, 8, 31, 0, 0)
    );

    List<ReviewAggregation> aggregations = new ArrayList<>();
    List<UUID> expectedTop10 = new ArrayList<>();

    for (int i = 1; i <= 12; i++) {
      UUID reviewId = UUID.randomUUID();

      aggregations.add(
          new ReviewAggregation(
              reviewId,
              100L - i,
              0L
          )
      );

      if (i <= 10) {
        expectedTop10.add(reviewId);
      }
    }

    when(periodResolver.resolve(
        PeriodType.WEEKLY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(aggregations);

    reviewRankingBatchService.generate(
        PeriodType.WEEKLY,
        baseDate
    );

    ArgumentCaptor<TopReviewRankedEvent> eventCaptor =
        ArgumentCaptor.forClass(TopReviewRankedEvent.class);

    verify(eventPublisher)
        .publishEvent(eventCaptor.capture());

    TopReviewRankedEvent event = eventCaptor.getValue();

    assertThat(event.periodType())
        .isEqualTo(PeriodType.WEEKLY);

    assertThat(event.topReviewIds())
        .containsExactlyElementsOf(expectedTop10);
  }

  @Test
  @DisplayName("월간 인기 리뷰는 TOP 10 이벤트를 발행한다")
  void publishMonthlyTopReviewEvent() {
    LocalDate baseDate = LocalDate.of(2026, 8, 30);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 1, 0, 0),
        LocalDateTime.of(2026, 8, 31, 0, 0)
    );

    UUID reviewId = UUID.randomUUID();

    when(periodResolver.resolve(
        PeriodType.MONTHLY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of(
            new ReviewAggregation(
                reviewId,
                10L,
                10L
            )
        ));

    reviewRankingBatchService.generate(
        PeriodType.MONTHLY,
        baseDate
    );

    ArgumentCaptor<TopReviewRankedEvent> eventCaptor =
        ArgumentCaptor.forClass(TopReviewRankedEvent.class);

    verify(eventPublisher)
        .publishEvent(eventCaptor.capture());

    TopReviewRankedEvent event = eventCaptor.getValue();

    assertThat(event.periodType())
        .isEqualTo(PeriodType.MONTHLY);

    assertThat(event.topReviewIds())
        .containsExactly(reviewId);
  }

  @Test
  @DisplayName("역대 인기 리뷰는 TOP 10 이벤트를 발행한다")
  void publishAllTimeTopReviewEvent() {
    LocalDate baseDate = LocalDate.of(2026, 8, 30);

    PeriodRange periodRange = new PeriodRange(
        null,
        LocalDateTime.of(2026, 8, 31, 0, 0)
    );

    UUID reviewId = UUID.randomUUID();

    when(periodResolver.resolve(
        PeriodType.ALL_TIME,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of(
            new ReviewAggregation(
                reviewId,
                20L,
                20L
            )
        ));

    reviewRankingBatchService.generate(
        PeriodType.ALL_TIME,
        baseDate
    );

    ArgumentCaptor<TopReviewRankedEvent> eventCaptor =
        ArgumentCaptor.forClass(TopReviewRankedEvent.class);

    verify(eventPublisher)
        .publishEvent(eventCaptor.capture());

    TopReviewRankedEvent event = eventCaptor.getValue();

    assertThat(event.periodType())
        .isEqualTo(PeriodType.ALL_TIME);

    assertThat(event.topReviewIds())
        .containsExactly(reviewId);
  }
}