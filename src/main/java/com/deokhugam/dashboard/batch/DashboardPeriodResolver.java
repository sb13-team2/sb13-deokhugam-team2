package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class DashboardPeriodResolver {

  public PeriodRange resolve(PeriodType periodType, LocalDate baseDate) {
    LocalDateTime endExclusive = baseDate
        .plusDays(1)
        .atStartOfDay();

    LocalDateTime startInclusive = switch (periodType) {
      case DAILY -> baseDate.atStartOfDay();
      case WEEKLY -> baseDate.minusDays(6).atStartOfDay();
      case MONTHLY -> baseDate.minusDays(29).atStartOfDay();
      case ALL_TIME -> null;
    };

    return new PeriodRange(startInclusive, endExclusive);
  }

  public record PeriodRange(
      LocalDateTime startInclusive,
      LocalDateTime endExclusive
  ) {
  }
}