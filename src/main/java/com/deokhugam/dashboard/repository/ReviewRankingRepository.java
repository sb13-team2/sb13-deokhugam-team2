package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.ReviewRanking;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRankingRepository extends JpaRepository<ReviewRanking, UUID> {

  @Query("SELECT MAX(rr.baseDate) FROM ReviewRanking rr WHERE rr.periodType = :periodType")
  Optional<LocalDate> findLatestBaseDate(@Param("periodType") PeriodType periodType);

  long countByPeriodTypeAndBaseDate(PeriodType periodType, LocalDate baseDate);

  @Query("""
      SELECT rr
      FROM ReviewRanking rr
      WHERE rr.periodType = :periodType
        AND rr.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR rr.ranking > :cursorRanking
          OR (rr.ranking = :cursorRanking AND rr.createdAt > :after)
        )
      ORDER BY rr.ranking ASC, rr.createdAt ASC
      """)
  List<ReviewRanking> findRankingPageAsc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Query("""
      SELECT rr
      FROM ReviewRanking rr
      WHERE rr.periodType = :periodType
        AND rr.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR rr.ranking < :cursorRanking
          OR (rr.ranking = :cursorRanking AND rr.createdAt < :after)
        )
      ORDER BY rr.ranking DESC, rr.createdAt DESC
      """)
  List<ReviewRanking> findRankingPageDesc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Modifying
  @Query("""
    DELETE FROM ReviewRanking rr
    WHERE rr.periodType = :periodType
      AND rr.baseDate = :baseDate
    """)
  void deleteSnapshot(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate
  );

  void deleteAllByReviewId(UUID reviewId);
}
