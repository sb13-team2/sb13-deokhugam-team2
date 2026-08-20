package com.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void 활성_리뷰가_존재하면_true를_반환한다() {
        User user = userRepository.save(createUser());
        Book book = bookRepository.save(createBook());

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );
        reviewRepository.saveAndFlush(review);

        boolean exists =
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        user.getId(),
                        book.getId()
                );

        assertThat(exists).isTrue();
    }

    @Test
    void 리뷰가_논리_삭제되면_활성_리뷰가_존재하지_않는다고_반환한다() {
        User user = userRepository.save(createUser());
        Book book = bookRepository.save(createBook());

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );
        review.softDelete();
        reviewRepository.saveAndFlush(review);

        boolean exists =
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        user.getId(),
                        book.getId()
                );

        assertThat(exists).isFalse();
    }

    @Test
    void ID로_활성_리뷰를_조회한다() {
        User user = userRepository.save(createUser());
        Book book = bookRepository.save(createBook());

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );
        Review savedReview = reviewRepository.saveAndFlush(review);

        Optional<Review> result =
                reviewRepository.findByIdAndDeletedAtIsNull(
                        savedReview.getId()
                );

        assertThat(result).contains(savedReview);
    }

    @Test
    void 논리_삭제된_리뷰는_ID로_활성_조회할_수_없다() {
        User user = userRepository.save(createUser());
        Book book = bookRepository.save(createBook());

        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );
        review.softDelete();
        Review savedReview = reviewRepository.saveAndFlush(review);

        Optional<Review> result =
                reviewRepository.findByIdAndDeletedAtIsNull(
                        savedReview.getId()
                );

        assertThat(result).isEmpty();
    }

    private User createUser() {
        return User.create(
                "reviewer@example.com",
                "리아",
                "encodedPassword"
        );
    }

    private Book createBook() {
        return new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 19),
                UUID.randomUUID().toString()
        );
    }
}