package com.deokhugam.dashboard.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DashboardScoreCalculatorTest {

  @Test
  @DisplayName("인기 도서 점수를 산출한다: (리뷰수 * 0.4) + (평점 평균 * 0.6)")
  void calculateBookScore() {
    long reviewCount = 10;
    double averageRating = 4.5;
    // 10 * 0.4(4.0) + 4.5 * 0.6(2.7) = 6.7

    double result = DashboardScoreCalculator.calculateBookScore(reviewCount, averageRating);

    assertThat(result).isEqualTo(6.7);
  }

  @Test
  @DisplayName("인기 리뷰 점수를 산출한다: (좋아요 수 * 0.3) + (댓글 수 * 0.7)")
  void calculateReviewScore() {
    long likeCount = 20;
    long commentCount = 5;
    // 20 * 0.3(6.0) + 5 * 0.7(3.5) = 9.5

    double result = DashboardScoreCalculator.calculateReviewScore(likeCount, commentCount);

    assertThat(result).isEqualTo(9.5);
  }

  @Test
  @DisplayName("파워 유저 활동 점수를 산출한다: (리뷰 인기점수 * 0.5) + (좋아요 수 * 0.2) + (댓글 수 * 0.3)")
  void calculatePowerUserScore() {
    double reviewPopularScore = 9.5;
    long likeCount = 10;
    long commentCount = 10;
    // 9.5 * 0.5(4.75) + 10 * 0.2(2.0) + 10 * 0.3(3.0) = 9.75

    double result = DashboardScoreCalculator.calculatePowerUserScore(reviewPopularScore, likeCount, commentCount);

    assertThat(result).isEqualTo(9.75);
  }
}