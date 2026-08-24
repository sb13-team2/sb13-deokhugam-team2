package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.dashboard.batch.BookRankingAggregationRepository.BookAggregation;
import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.entity.BookRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.repository.BookRankingRepository;
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
class BookRankingBatchServiceTest {

  @Mock
  private DashboardPeriodResolver periodResolver;

  @Mock
  private BookRankingAggregationRepository aggregationRepository;

  @Mock
  private BookRankingRepository bookRankingRepository;

  @InjectMocks
  private BookRankingBatchService bookRankingBatchService;

  @Captor
  private ArgumentCaptor<List<BookRanking>> rankingsCaptor;

  @Test
  @DisplayName("인기 도서 점수 순으로 랭킹을 생성한다")
  void generateBookRanking() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 24, 0, 0),
        LocalDateTime.of(2026, 8, 25, 0, 0)
    );

    UUID firstBookId =
        UUID.fromString("00000000-0000-0000-0000-000000000001");

    UUID secondBookId =
        UUID.fromString("00000000-0000-0000-0000-000000000002");

    UUID thirdBookId =
        UUID.fromString("00000000-0000-0000-0000-000000000003");

    when(periodResolver.resolve(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of(
            // 10 * 0.4 + 4.5 * 0.6 = 6.7
            new BookAggregation(
                firstBookId,
                10L,
                4.5
            ),

            // 12 * 0.4 + 3.0 * 0.6 = 6.6
            new BookAggregation(
                secondBookId,
                12L,
                3.0
            ),

            // 5 * 0.4 + 5.0 * 0.6 = 5.0
            new BookAggregation(
                thirdBookId,
                5L,
                5.0
            )
        ));

    bookRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    InOrder inOrder = inOrder(bookRankingRepository);

    inOrder.verify(bookRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    inOrder.verify(bookRankingRepository)
        .saveAll(rankingsCaptor.capture());

    List<BookRanking> rankings =
        rankingsCaptor.getValue();

    assertThat(rankings).hasSize(3);

    assertThat(rankings)
        .extracting(BookRanking::getBookId)
        .containsExactly(
            firstBookId,
            secondBookId,
            thirdBookId
        );

    assertThat(rankings)
        .extracting(BookRanking::getRanking)
        .containsExactly(
            1L,
            2L,
            3L
        );

    assertThat(rankings)
        .extracting(BookRanking::getScore)
        .containsExactly(
            6.7,
            6.6,
            5.0
        );

    assertThat(rankings.get(0).getPeriodType())
        .isEqualTo(PeriodType.DAILY);

    assertThat(rankings.get(0).getBaseDate())
        .isEqualTo(baseDate);

    assertThat(rankings.get(0).getReviewCount())
        .isEqualTo(10L);

    assertThat(rankings.get(0).getRating())
        .isEqualTo(4.5);
  }

  @Test
  @DisplayName("집계 결과가 없어도 기존 랭킹 스냅샷을 삭제한다")
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

    bookRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    verify(bookRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    verify(bookRankingRepository)
        .saveAll(List.of());
  }
}