package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.dashboard.batch.BookRankingAggregationRepository.BookAggregation;
import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    JpaConfig.class,
    BookRankingAggregationRepository.class
})
class BookRankingAggregationRepositoryTest {

  @Autowired
  private BookRankingAggregationRepository aggregationRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("기간 내 리뷰 수와 평균 평점을 도서별로 집계한다")
  void aggregateBookReviews() {
    User user = userRepository.save(
        User.create(
            "batch@example.com",
            "batchUser",
            "encodedPassword"
        )
    );

    Book firstBook = bookRepository.save(createBook());
    Book secondBook = bookRepository.save(createBook());

    reviewRepository.save(
        Review.create(
            user,
            firstBook,
            "첫 번째 리뷰",
            5
        )
    );

    reviewRepository.save(
        Review.create(
            user,
            firstBook,
            "두 번째 리뷰",
            3
        )
    );

    reviewRepository.save(
        Review.create(
            user,
            secondBook,
            "세 번째 리뷰",
            5
        )
    );

    reviewRepository.flush();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.minusMinutes(1),
        now.plusMinutes(1)
    );

    List<BookAggregation> result =
        aggregationRepository.aggregate(periodRange);

    Map<UUID, BookAggregation> resultMap =
        result.stream()
            .collect(Collectors.toMap(
                BookAggregation::bookId,
                Function.identity()
            ));

    assertThat(resultMap).hasSize(2);

    BookAggregation firstAggregation =
        resultMap.get(firstBook.getId());

    assertThat(firstAggregation.reviewCount())
        .isEqualTo(2L);

    assertThat(firstAggregation.averageRating())
        .isEqualTo(4.0);

    BookAggregation secondAggregation =
        resultMap.get(secondBook.getId());

    assertThat(secondAggregation.reviewCount())
        .isEqualTo(1L);

    assertThat(secondAggregation.averageRating())
        .isEqualTo(5.0);
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