package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReviewRankingRepository extends JpaRepository<ReviewRanking, UUID> {
  List<ReviewRanking> findAllByPeriodTypeAndBaseDateOrderByRankingAsc(PeriodType periodType, LocalDate baseDate);
}