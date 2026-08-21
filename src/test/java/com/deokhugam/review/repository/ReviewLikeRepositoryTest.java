package com.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.entity.ReviewLike;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class ReviewLikeRepositoryTest {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void 사용자가_리뷰에_좋아요를_누르면_true를_반환한다() {
        User author = userRepository.save(
                createUser("author@example.com", "작성자")
        );
        User requester = userRepository.save(
                createUser("requester@example.com", "요청자")
        );
        Book book = bookRepository.save(createBook());

        Review review = reviewRepository.save(
                Review.create(
                        author,
                        book,
                        "좋은 책입니다.",
                        5
                )
        );

        reviewLikeRepository.saveAndFlush(
                ReviewLike.create(review, requester)
        );

        boolean likedByMe =
                reviewLikeRepository.existsByReviewIdAndUserId(
                        review.getId(),
                        requester.getId()
                );

        assertThat(likedByMe).isTrue();
    }

    @Test
    void 사용자가_리뷰에_좋아요를_누르지_않으면_false를_반환한다() {
        User author = userRepository.save(
                createUser("author@example.com", "작성자")
        );
        User requester = userRepository.save(
                createUser("requester@example.com", "요청자")
        );
        Book book = bookRepository.save(createBook());

        Review review = reviewRepository.saveAndFlush(
                Review.create(
                        author,
                        book,
                        "좋은 책입니다.",
                        5
                )
        );

        boolean likedByMe =
                reviewLikeRepository.existsByReviewIdAndUserId(
                        review.getId(),
                        requester.getId()
                );

        assertThat(likedByMe).isFalse();
    }

    @Test
    void 리뷰와_사용자_ID로_좋아요를_조회한다() {
        User author = userRepository.save(
                createUser("author@example.com", "작성자")
        );
        User requester = userRepository.save(
                createUser("requester@example.com", "요청자")
        );
        Book book = bookRepository.save(createBook());

        Review review = reviewRepository.save(
                Review.create(
                        author,
                        book,
                        "좋은 책입니다.",
                        5
                )
        );

        ReviewLike savedReviewLike =
                reviewLikeRepository.saveAndFlush(
                        ReviewLike.create(
                                review,
                                requester
                        )
                );

        Optional<ReviewLike> result =
                reviewLikeRepository.findByReviewIdAndUserId(
                        review.getId(),
                        requester.getId()
                );

        assertThat(result).contains(savedReviewLike);
    }

    private User createUser(
            String email,
            String nickname
    ) {
        return User.create(
                email,
                nickname,
                "encodedPassword"
        );
    }

    private Book createBook() {
        return new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 20),
                UUID.randomUUID().toString()
        );
    }
}