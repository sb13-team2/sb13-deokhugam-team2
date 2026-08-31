package com.deokhugam.dashboard.event;

import com.deokhugam.dashboard.entity.PeriodType;
import java.util.List;
import java.util.UUID;

public record TopReviewRankedEvent(
    List<UUID> topReviewIds,
    PeriodType periodType
) { }
