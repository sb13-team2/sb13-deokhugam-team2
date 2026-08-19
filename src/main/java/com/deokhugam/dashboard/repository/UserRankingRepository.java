package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.UserRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UserRankingRepository extends JpaRepository<UserRanking, UUID> {
  List<UserRanking> findAllByPeriodTypeAndBaseDateOrderByRankingAsc(PeriodType periodType, LocalDate baseDate);
}