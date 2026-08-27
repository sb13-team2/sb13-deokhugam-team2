package com.deokhugam.dashboard.batch;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardBatchScheduler {

  private static final String ZONE = "Asia/Seoul";

  private final BookRankingBatchService bookRankingBatchService;
  private final ReviewRankingBatchService reviewRankingBatchService;
  private final UserRankingBatchService userRankingBatchService;

  @Scheduled(cron = "0 5 0 * * *", zone = ZONE)
  public void runDailyBatch() {
    LocalDate baseDate =
        LocalDate.now(ZoneId.of(ZONE)).minusDays(1);

    runBatch(baseDate);
  }

  void runBatch(LocalDate baseDate) {
    bookRankingBatchService.generateAll(baseDate);
    reviewRankingBatchService.generateAll(baseDate);
    userRankingBatchService.generateAll(baseDate);
  }
}