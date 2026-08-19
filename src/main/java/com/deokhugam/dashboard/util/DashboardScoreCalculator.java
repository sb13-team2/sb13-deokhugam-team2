package com.deokhugam.dashboard.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DashboardScoreCalculator {

  // 도서 가중치
  private static final double BOOK_REVIEW_WEIGHT = 0.4;
  private static final double BOOK_RATING_WEIGHT = 0.6;

  // 리뷰 가중치
  private static final double REVIEW_LIKE_WEIGHT = 0.3;
  private static final double REVIEW_COMMENT_WEIGHT = 0.7;

  // 유저 활동 가중치
  private static final double USER_REVIEW_SCORE_WEIGHT = 0.5;
  private static final double USER_LIKE_WEIGHT = 0.2;
  private static final double USER_COMMENT_WEIGHT = 0.3;

  // 객체 생성 방지
  private DashboardScoreCalculator() {
    throw new IllegalStateException("Utility class");
  }

  public static double calculateBookScore(long reviewCount, double averageRating) {
    double rawScore = (reviewCount * BOOK_REVIEW_WEIGHT) + (averageRating * BOOK_RATING_WEIGHT);
    return roundToTwoDecimalPlaces(rawScore);
  }

  public static double calculateReviewScore(long likeCount, long commentCount) {
    double rawScore = (likeCount * REVIEW_LIKE_WEIGHT) + (commentCount * REVIEW_COMMENT_WEIGHT);
    return roundToTwoDecimalPlaces(rawScore);
  }

  public static double calculatePowerUserScore(double reviewPopularScore, long likeCount, long commentCount) {
    double rawScore = (reviewPopularScore * USER_REVIEW_SCORE_WEIGHT) +
        (likeCount * USER_LIKE_WEIGHT) +
        (commentCount * USER_COMMENT_WEIGHT);
    return roundToTwoDecimalPlaces(rawScore);
  }

  // 부동소수점 오차 해결을 위한 소수점 둘째 자리 반올림
  private static double roundToTwoDecimalPlaces(double value) {
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .doubleValue();
  }
}