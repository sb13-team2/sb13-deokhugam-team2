package com.deokhugam.dashboard.batch;

import static org.mockito.Mockito.inOrder;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardBatchSchedulerTest {

  @Mock
  private BookRankingBatchService bookRankingBatchService;

  @Mock
  private ReviewRankingBatchService reviewRankingBatchService;

  @Mock
  private UserRankingBatchService userRankingBatchService;

  @InjectMocks
  private DashboardBatchScheduler dashboardBatchScheduler;

  @Test
  @DisplayName("대시보드 랭킹 배치를 순서대로 실행한다")
  void runDashboardBatch() {
    LocalDate baseDate =
        LocalDate.of(2026, 8, 27);

    dashboardBatchScheduler.runBatch(baseDate);

    InOrder inOrder = inOrder(
        bookRankingBatchService,
        reviewRankingBatchService,
        userRankingBatchService
    );

    inOrder.verify(bookRankingBatchService)
        .generateAll(baseDate);

    inOrder.verify(reviewRankingBatchService)
        .generateAll(baseDate);

    inOrder.verify(userRankingBatchService)
        .generateAll(baseDate);
  }
}