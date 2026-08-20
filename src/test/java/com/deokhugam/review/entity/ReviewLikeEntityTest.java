package com.deokhugam.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.user.entity.User;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReviewLikeEntityTest {

    @Test
    void 리뷰_좋아요를_생성한다() {
        User user = User.create(
                "liker@example.com",
                "좋아요사용자",
                "encodedPassword"
        );

        Book book = new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 20),
                "9781234567890"
        );

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );

        ReviewLike reviewLike = ReviewLike.create(
                review,
                user
        );

        assertThat(reviewLike.getReview()).isEqualTo(review);
        assertThat(reviewLike.getUser()).isEqualTo(user);
    }
}