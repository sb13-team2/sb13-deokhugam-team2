package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DashboardPeriodResolverTest {

  private final DashboardPeriodResolver periodResolver =
      new DashboardPeriodResolver();

  @Test
  @DisplayName("일간 기간은 기준일 하루로 계산한다")
  void resolveDailyPeriod() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange result =
        periodResolver.resolve(PeriodType.DAILY, baseDate);

    assertThat(result.startInclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 24, 0, 0));

    assertThat(result.endExclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 25, 0, 0));
  }

  @Test
  @DisplayName("주간 기간은 기준일을 포함한 최근 7일로 계산한다")
  void resolveWeeklyPeriod() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange result =
        periodResolver.resolve(PeriodType.WEEKLY, baseDate);

    assertThat(result.startInclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 18, 0, 0));

    assertThat(result.endExclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 25, 0, 0));
  }

  @Test
  @DisplayName("월간 기간은 기준일을 포함한 최근 30일로 계산한다")
  void resolveMonthlyPeriod() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange result =
        periodResolver.resolve(PeriodType.MONTHLY, baseDate);

    assertThat(result.startInclusive())
        .isEqualTo(LocalDateTime.of(2026, 7, 26, 0, 0));

    assertThat(result.endExclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 25, 0, 0));
  }

  @Test
  @DisplayName("역대 기간은 시작일 없이 기준일까지 계산한다")
  void resolveAllTimePeriod() {
    LocalDate baseDate = LocalDate.of(2026, 8, 24);

    PeriodRange result =
        periodResolver.resolve(PeriodType.ALL_TIME, baseDate);

    assertThat(result.startInclusive()).isNull();

    assertThat(result.endExclusive())
        .isEqualTo(LocalDateTime.of(2026, 8, 25, 0, 0));
  }
}