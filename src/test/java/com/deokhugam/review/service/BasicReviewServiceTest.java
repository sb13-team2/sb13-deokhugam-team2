package com.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.exception.DuplicateReviewException;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.exception.UserException;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BasicReviewService reviewService;

    @Test
    void 활성_리뷰가_없으면_리뷰를_등록한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        User user = createUser(userId);
        Book book = createBook(bookId);

        given(
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        userId,
                        bookId
                )
        ).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(user));
        given(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .willReturn(Optional.of(book));
        given(reviewRepository.save(any(Review.class)))
                .willAnswer(invocation -> {
                    Review review = invocation.getArgument(0);
                    ReflectionTestUtils.setField(review, "id", reviewId);
                    ReflectionTestUtils.setField(
                            review,
                            "createdAt",
                            createdAt
                    );
                    ReflectionTestUtils.setField(
                            review,
                            "updatedAt",
                            createdAt
                    );
                    return review;
                });

        ReviewDetailResponse response = reviewService.create(request);

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.bookId()).isEqualTo(bookId);
        assertThat(response.content()).isEqualTo("좋은 책입니다.");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.likeCount()).isZero();
        assertThat(response.commentCount()).isZero();
        assertThat(response.likedByMe()).isFalse();
        assertThat(response.createdAt()).isEqualTo(createdAt);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void 활성_리뷰가_이미_존재하면_등록에_실패한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        given(
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        userId,
                        bookId
                )
        ).willReturn(true);

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(DuplicateReviewException.class);

        verify(userRepository, never())
                .findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(bookRepository, never())
                .findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void 사용자가_존재하지_않으면_등록에_실패한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        given(
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        userId,
                        bookId
                )
        ).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(UserException.class);

        verify(bookRepository, never())
                .findByIdAndDeletedAtIsNull(any(UUID.class));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void 도서가_존재하지_않으면_등록에_실패한다() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        User user = createUser(userId);

        given(
                reviewRepository.existsByUserIdAndBookIdAndDeletedAtIsNull(
                        userId,
                        bookId
                )
        ).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId))
                .willReturn(Optional.of(user));
        given(bookRepository.findByIdAndDeletedAtIsNull(bookId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(BookNotFoundException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    private User createUser(UUID userId) {
        User user = User.create(
                "reviewer@example.com",
                "리아",
                "encodedPassword"
        );
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Book createBook(UUID bookId) {
        Book book = new Book(
                "테스트 도서",
                "테스트 저자",
                "테스트 설명",
                "테스트 출판사",
                LocalDate.of(2026, 8, 19),
                "9781234567890"
        );
        ReflectionTestUtils.setField(book, "id", bookId);
        return book;
    }
}