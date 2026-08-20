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
import com.deokhugam.global.storage.Storage;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.exception.DuplicateReviewException;
import com.deokhugam.review.exception.ReviewAccessDeniedException;
import com.deokhugam.review.exception.ReviewNotFoundException;
import com.deokhugam.review.repository.ReviewLikeRepository;
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
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Storage storage;

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

    @Test
    void 작성자가_자신의_리뷰를_수정하면_갱신된_시간을_반환한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        LocalDateTime updatedAtBefore =
                LocalDateTime.now().minusHours(1);
        LocalDateTime updatedAtAfter =
                LocalDateTime.now();

        User user = createUser(userId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, user, book);

        ReflectionTestUtils.setField(
                review,
                "updatedAt",
                updatedAtBefore
        );

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        given(reviewRepository.saveAndFlush(review))
                .willAnswer(invocation -> {
                    Review savedReview = invocation.getArgument(0);
                    ReflectionTestUtils.setField(
                            savedReview,
                            "updatedAt",
                            updatedAtAfter
                    );
                    return savedReview;
                });

        ReviewDetailResponse response = reviewService.update(
                reviewId,
                userId,
                request
        );

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.content())
                .isEqualTo("수정한 리뷰 내용입니다.");
        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.updatedAt()).isEqualTo(updatedAtAfter);
        assertThat(response.updatedAt()).isNotEqualTo(updatedAtBefore);

        verify(reviewRepository).saveAndFlush(review);
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_수정에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.update(
                reviewId,
                userId,
                request
        )).isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 다른_사용자의_리뷰는_수정할_수_없다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.update(
                reviewId,
                requesterId,
                request
        )).isInstanceOf(ReviewAccessDeniedException.class);

        assertThat(review.getContent()).isEqualTo("좋은 책입니다.");
        assertThat(review.getRating()).isEqualTo(5);
    }

    @Test
    void 리뷰_등록_응답은_접근_가능한_썸네일_URL을_반환한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        String thumbnailPath = "book-thumbnails/test.jpg";
        String accessibleThumbnailUrl =
                "https://example.com/book-thumbnails/test.jpg";

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        User user = createUser(userId);
        Book book = createBook(bookId);
        book.updateThumbnailUrl(thumbnailPath);

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

        given(storage.getUrl(thumbnailPath))
                .willReturn(accessibleThumbnailUrl);

        given(reviewRepository.save(any(Review.class)))
                .willAnswer(invocation -> {
                    Review review = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            review,
                            "id",
                            reviewId
                    );
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

        assertThat(response.bookThumbnailUrl())
                .isEqualTo(accessibleThumbnailUrl);

        verify(storage).getUrl(thumbnailPath);
    }

    @Test
    void 좋아요한_사용자가_리뷰를_상세_조회하면_likedByMe는_true이다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        given(reviewLikeRepository.existsByReviewIdAndUserId(
                reviewId,
                requesterId
        )).willReturn(true);

        ReviewDetailResponse response = reviewService.findById(
                reviewId,
                requesterId
        );

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.bookId()).isEqualTo(bookId);
        assertThat(response.userId()).isEqualTo(authorId);
        assertThat(response.content()).isEqualTo("좋은 책입니다.");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.likedByMe()).isTrue();
    }

    @Test
    void 좋아요하지_않은_사용자가_리뷰를_상세_조회하면_likedByMe는_false이다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        given(reviewLikeRepository.existsByReviewIdAndUserId(
                reviewId,
                requesterId
        )).willReturn(false);

        ReviewDetailResponse response = reviewService.findById(
                reviewId,
                requesterId
        );

        assertThat(response.id()).isEqualTo(reviewId);
        assertThat(response.likedByMe()).isFalse();
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_상세_조회에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.findById(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewNotFoundException.class);

        verify(reviewLikeRepository, never())
                .existsByReviewIdAndUserId(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void 좋아요한_사용자가_리뷰를_수정하면_likedByMe는_true이다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User user = createUser(userId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, user, book);

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        given(reviewRepository.saveAndFlush(review))
                .willReturn(review);

        given(reviewLikeRepository.existsByReviewIdAndUserId(
                reviewId,
                userId
        )).willReturn(true);

        ReviewDetailResponse response = reviewService.update(
                reviewId,
                userId,
                request
        );

        assertThat(response.likedByMe()).isTrue();
    }

    private Review createReview(
            UUID reviewId,
            User user,
            Book book
    ) {
        Review review = Review.create(
                user,
                book,
                "좋은 책입니다.",
                5
        );
        ReflectionTestUtils.setField(review, "id", reviewId);
        ReflectionTestUtils.setField(
                review,
                "createdAt",
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(
                review,
                "updatedAt",
                LocalDateTime.now()
        );
        return review;
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