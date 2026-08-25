package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.ReviewRankingAggregationRepository.ReviewAggregation;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    JpaConfig.class,
    ReviewRankingAggregationRepository.class
})
class ReviewRankingAggregationRepositoryTest {

  @Autowired
  private ReviewRankingAggregationRepository aggregationRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("기간 내 활성 리뷰만 랭킹 집계 대상으로 조회한다")
  void aggregateActiveReviews() {
    User user = userRepository.save(createUser());

    Book activeReviewBook =
        bookRepository.save(createBook());

    Book deletedReviewBook =
        bookRepository.save(createBook());

    Review activeReview = reviewRepository.save(
        Review.create(
            user,
            activeReviewBook,
            "활성 리뷰",
            5
        )
    );

    Review deletedReview = Review.create(
        user,
        deletedReviewBook,
        "삭제된 리뷰",
        4
    );

    deletedReview.softDelete();
    reviewRepository.save(deletedReview);

    reviewRepository.flush();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.minusMinutes(1),
        now.plusMinutes(1)
    );

    List<ReviewAggregation> result =
        aggregationRepository.aggregate(periodRange);

    assertThat(result)
        .extracting(ReviewAggregation::reviewId)
        .containsExactly(activeReview.getId());

    assertThat(result.get(0).likeCount())
        .isZero();

    assertThat(result.get(0).commentCount())
        .isZero();
  }

  @Test
  @DisplayName("기간 밖에 생성된 리뷰는 랭킹 집계에서 제외한다")
  void excludeReviewsOutsidePeriod() {
    User user = userRepository.save(createUser());

    Book book = bookRepository.save(createBook());

    reviewRepository.save(
        Review.create(
            user,
            book,
            "테스트 리뷰",
            5
        )
    );

    reviewRepository.flush();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.plusDays(1),
        now.plusDays(2)
    );

    List<ReviewAggregation> result =
        aggregationRepository.aggregate(periodRange);

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("실제 댓글 수를 직접 집계하고 논리 삭제된 댓글도 포함한다")
  void aggregateActualCommentCount() {
    User user = userRepository.save(createUser());

    Book book = bookRepository.save(createBook());

    Review review = reviewRepository.save(
        Review.create(
            user,
            book,
            "댓글 집계 테스트 리뷰",
            5
        )
    );

    Comment activeComment = new Comment(
        "활성 댓글",
        user.getId(),
        review.getId()
    );

    commentRepository.save(activeComment);

    Comment deletedComment = new Comment(
        "삭제된 댓글",
        user.getId(),
        review.getId()
    );

    deletedComment.softDelete();
    commentRepository.save(deletedComment);

    commentRepository.flush();
    reviewRepository.flush();

    assertThat(review.getCommentCount()).isZero();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.minusMinutes(1),
        now.plusMinutes(1)
    );

    List<ReviewAggregation> result =
        aggregationRepository.aggregate(periodRange);

    assertThat(result).hasSize(1);

    ReviewAggregation aggregation = result.get(0);

    assertThat(aggregation.reviewId())
        .isEqualTo(review.getId());

    assertThat(aggregation.commentCount())
        .isEqualTo(2L);
  }

  private User createUser() {
    return User.create(
        "batch-" + UUID.randomUUID() + "@example.com",
        "batchUser",
        "encodedPassword"
    );
  }

  private Book createBook() {
    return new Book(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 24),
        UUID.randomUUID().toString()
    );
  }
}