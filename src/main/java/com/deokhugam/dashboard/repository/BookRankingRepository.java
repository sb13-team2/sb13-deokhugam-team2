package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.BookRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookRankingRepository extends JpaRepository<BookRanking, UUID> {
  List<BookRanking> findAllByPeriodTypeAndBaseDateOrderByRankingAsc(PeriodType periodType, LocalDate baseDate);
}