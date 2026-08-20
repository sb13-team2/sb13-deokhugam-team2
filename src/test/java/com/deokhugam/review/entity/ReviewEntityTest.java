package com.deokhugam.review.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.user.entity.User;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReviewEntityTest {

    @Test
    void 리뷰를_생성한다() {
        User user = User.create(
                "reviewer@example.com",
                "리아",
                "encodedPassword"
        );

        Book book = new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 19),
                "9781234567890"
        );

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );

        assertThat(review.getUser()).isEqualTo(user);
        assertThat(review.getBook()).isEqualTo(book);
        assertThat(review.getContent()).isEqualTo("좋은 책입니다.");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getLikeCount()).isZero();
        assertThat(review.getCommentCount()).isZero();
        assertThat(review.isDeleted()).isFalse();
    }

    @Test
    void 리뷰의_내용과_평점을_수정한다() {
        User user = User.create(
                "reviewer@example.com",
                "리아",
                "encodedPassword"
        );

        Book book = new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 19),
                "9781234567890"
        );

        Review review = Review.create(
                user,
                book,
                "수정 전 내용",
                3
        );

        review.update("수정 후 내용", 5);

        assertThat(review.getContent()).isEqualTo("수정 후 내용");
        assertThat(review.getRating()).isEqualTo(5);
    }
}