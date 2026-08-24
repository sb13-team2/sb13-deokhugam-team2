package com.deokhugam.dashboard.repository;

import com.deokhugam.dashboard.entity.BookRanking;
import com.deokhugam.dashboard.entity.PeriodType;
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

public interface BookRankingRepository extends JpaRepository<BookRanking, UUID> {

  @Query("SELECT MAX(br.baseDate) FROM BookRanking br WHERE br.periodType = :periodType")
  Optional<LocalDate> findLatestBaseDate(@Param("periodType") PeriodType periodType);

  long countByPeriodTypeAndBaseDate(PeriodType periodType, LocalDate baseDate);

  @Query("""
      SELECT br
      FROM BookRanking br
      WHERE br.periodType = :periodType
        AND br.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR br.ranking > :cursorRanking
          OR (br.ranking = :cursorRanking AND br.createdAt > :after)
        )
      ORDER BY br.ranking ASC, br.createdAt ASC
      """)
  List<BookRanking> findRankingPageAsc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Query("""
      SELECT br
      FROM BookRanking br
      WHERE br.periodType = :periodType
        AND br.baseDate = :baseDate
        AND (
          :cursorRanking IS NULL
          OR br.ranking < :cursorRanking
          OR (br.ranking = :cursorRanking AND br.createdAt < :after)
        )
      ORDER BY br.ranking DESC, br.createdAt DESC
      """)
  List<BookRanking> findRankingPageDesc(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate,
      @Param("cursorRanking") Long cursorRanking,
      @Param("after") LocalDateTime after,
      Pageable pageable
  );

  @Modifying
  @Query("""
    DELETE FROM BookRanking br
    WHERE br.periodType = :periodType
      AND br.baseDate = :baseDate
    """)
  void deleteSnapshot(
      @Param("periodType") PeriodType periodType,
      @Param("baseDate") LocalDate baseDate
  );

  void deleteAllByBookId(UUID bookId);
}
