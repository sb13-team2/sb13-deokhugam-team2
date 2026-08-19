package com.deokhugam.dashboard.entity;

import com.deokhugam.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "review_ranking",
    indexes = {
        @Index(name = "idx_review_ranking_period_date", columnList = "period_type, base_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewRanking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID reviewId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  private PeriodType periodType;

  @Column(nullable = false)
  private int ranking;

  @Column(nullable = false)
  private double score;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Builder
  public ReviewRanking(UUID reviewId, PeriodType periodType, int ranking, double score, LocalDate baseDate) {
    this.reviewId = reviewId;
    this.periodType = periodType;
    this.ranking = ranking;
    this.score = score;
    this.baseDate = baseDate;
  }
}