package com.deokhugam.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.global.storage.Storage;
import com.deokhugam.notification.entity.NotificationType;
import com.deokhugam.notification.repository.NotificationRepository;
import com.deokhugam.notification.service.NotificationService;
import com.deokhugam.review.dto.ReviewCursor;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.entity.ReviewLike;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
    private CommentRepository commentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ReviewRankingRepository reviewRankingRepository;

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

    @Test
    void 작성자가_자신의_리뷰를_논리_삭제한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User user = createUser(userId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, user, book);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        reviewService.softDelete(
                reviewId,
                userId
        );

        assertThat(review.isDeleted()).isTrue();
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_논리_삭제에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.softDelete(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 다른_사용자의_리뷰는_논리_삭제할_수_없다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        given(reviewRepository.findByIdAndDeletedAtIsNull(reviewId))
                .willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.softDelete(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewAccessDeniedException.class);

        assertThat(review.isDeleted()).isFalse();
    }

    @Test
    void 좋아요가_없으면_좋아요를_추가한다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        User requester = createUser(requesterId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        given(reviewRepository.findByIdForUpdate(reviewId))
                .willReturn(Optional.of(review));

        given(reviewLikeRepository.findByReviewIdAndUserId(
                reviewId,
                requesterId
        )).willReturn(Optional.empty());

        given(userRepository.findByIdAndDeletedAtIsNull(requesterId))
                .willReturn(Optional.of(requester));

        ReviewLikeResponse response = reviewService.toggleLike(
                reviewId,
                requesterId
        );

        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.userId()).isEqualTo(requesterId);
        assertThat(response.liked()).isTrue();
        assertThat(review.getLikeCount()).isEqualTo(1);

        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(notificationService).createNotification(
                author,
                review,
                requester.getNickname() + "님이 좋아요를 눌렀습니다.",
                NotificationType.REVIEW_LIKE
        );
    }

    @Test
    void 좋아요가_있으면_좋아요를_취소한다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        User requester = createUser(requesterId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);
        review.increaseLikeCount();

        ReviewLike reviewLike = ReviewLike.create(
                review,
                requester
        );

        given(reviewRepository.findByIdForUpdate(reviewId))
                .willReturn(Optional.of(review));

        given(userRepository.findByIdAndDeletedAtIsNull(requesterId))
                .willReturn(Optional.of(requester));

        given(reviewLikeRepository.findByReviewIdAndUserId(
                reviewId,
                requesterId
        )).willReturn(Optional.of(reviewLike));

        ReviewLikeResponse response = reviewService.toggleLike(
                reviewId,
                requesterId
        );

        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.userId()).isEqualTo(requesterId);
        assertThat(response.liked()).isFalse();
        assertThat(review.getLikeCount()).isZero();

        verify(reviewLikeRepository).delete(reviewLike);
        verifyNoInteractions(notificationService);
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_좋아요에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewRepository.findByIdForUpdate(reviewId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.toggleLike(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewNotFoundException.class);

        verify(reviewLikeRepository, never())
                .findByReviewIdAndUserId(
                        any(UUID.class),
                        any(UUID.class)
                );
    }

    @Test
    void 요청_사용자가_존재하지_않으면_좋아요_토글에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(reviewId, author, book);

        given(reviewRepository.findByIdForUpdate(reviewId))
                .willReturn(Optional.of(review));

        given(userRepository.findByIdAndDeletedAtIsNull(requesterId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.toggleLike(
                reviewId,
                requesterId
        )).isInstanceOf(UserException.class);

        verify(reviewLikeRepository, never())
                .findByReviewIdAndUserId(
                        any(UUID.class),
                        any(UUID.class)
                );

        verify(reviewLikeRepository, never())
                .save(any(ReviewLike.class));
    }

    @Test
    void 리뷰_목록을_조회하면_커서와_좋아요_여부를_반환한다() {
        UUID requesterId = UUID.randomUUID();

        UUID firstReviewId = UUID.randomUUID();
        UUID secondReviewId = UUID.randomUUID();
        UUID thirdReviewId = UUID.randomUUID();

        LocalDateTime firstCreatedAt =
                LocalDateTime.of(2026, 8, 21, 11, 0);
        LocalDateTime secondCreatedAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);
        LocalDateTime thirdCreatedAt =
                LocalDateTime.of(2026, 8, 21, 9, 0);

        User firstAuthor = createUser(UUID.randomUUID());
        User secondAuthor = createUser(UUID.randomUUID());
        User thirdAuthor = createUser(UUID.randomUUID());

        Book firstBook = createBook(UUID.randomUUID());
        Book secondBook = createBook(UUID.randomUUID());
        Book thirdBook = createBook(UUID.randomUUID());

        String thumbnailPath = "book-thumbnails/test.jpg";
        String accessibleThumbnailUrl =
                "https://example.com/book-thumbnails/test.jpg";

        firstBook.updateThumbnailUrl(thumbnailPath);

        Review firstReview = createReview(
                firstReviewId,
                firstAuthor,
                firstBook
        );
        firstReview.update("평점 5점 리뷰", 5);
        ReflectionTestUtils.setField(
                firstReview,
                "createdAt",
                firstCreatedAt
        );

        Review secondReview = createReview(
                secondReviewId,
                secondAuthor,
                secondBook
        );
        secondReview.update("평점 4점 리뷰", 4);
        ReflectionTestUtils.setField(
                secondReview,
                "createdAt",
                secondCreatedAt
        );

        Review thirdReview = createReview(
                thirdReviewId,
                thirdAuthor,
                thirdBook
        );
        thirdReview.update("평점 3점 리뷰", 3);
        ReflectionTestUtils.setField(
                thirdReview,
                "createdAt",
                thirdCreatedAt
        );

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                null,
                null,
                2
        );

        given(reviewRepository.findAllByCursor(request))
                .willReturn(List.of(
                        firstReview,
                        secondReview,
                        thirdReview
                ));

        given(reviewRepository.countAll(request))
                .willReturn(3L);

        given(reviewLikeRepository.findLikedReviewIds(
                requesterId,
                Set.of(firstReviewId, secondReviewId)
        )).willReturn(Set.of(secondReviewId));

        given(storage.getUrl(thumbnailPath))
                .willReturn(accessibleThumbnailUrl);

        ReviewListResponse response = reviewService.findAll(
                request,
                requesterId
        );

        assertThat(response.content()).hasSize(2);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(3L);
        assertThat(response.hasNext()).isTrue();

        ReviewCursor nextCursor =
                ReviewCursor.decode(response.nextCursor());

        assertThat(nextCursor.sortValue()).isEqualTo("4");
        assertThat(nextCursor.reviewId()).isEqualTo(secondReviewId);
        assertThat(response.nextAfter()).isEqualTo(secondCreatedAt);

        assertThat(response.content().get(0).id())
                .isEqualTo(firstReviewId);
        assertThat(response.content().get(0).bookThumbnailUrl())
                .isEqualTo(accessibleThumbnailUrl);
        assertThat(response.content().get(0).likedByMe())
                .isFalse();

        assertThat(response.content().get(1).id())
                .isEqualTo(secondReviewId);
        assertThat(response.content().get(1).likedByMe())
                .isTrue();

        verify(storage).getUrl(thumbnailPath);
    }

    @Test
    void 리뷰_목록이_비어있으면_다음_커서를_반환하지_않는다() {
        UUID requesterId = UUID.randomUUID();

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        given(reviewRepository.findAllByCursor(request))
                .willReturn(List.of());

        given(reviewRepository.countAll(request))
                .willReturn(0L);

        ReviewListResponse response = reviewService.findAll(
                request,
                requesterId
        );

        assertThat(response.content()).isEmpty();
        assertThat(response.size()).isZero();
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();

        verify(reviewLikeRepository, never())
                .findLikedReviewIds(
                        any(UUID.class),
                        anyCollection()
                );
    }

    @Test
    void 작성자가_리뷰를_물리_삭제하면_관련_데이터도_삭제한다() {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User user = createUser(userId);
        Book book = createBook(bookId);
        Review review = createReview(
                reviewId,
                user,
                book
        );

        review.softDelete();

        given(reviewRepository.findById(reviewId))
                .willReturn(Optional.of(review));

        reviewService.hardDelete(
                reviewId,
                userId
        );

        InOrder deletionOrder = inOrder(
                commentRepository,
                notificationRepository,
                reviewLikeRepository,
                reviewRankingRepository,
                reviewRepository
        );

        deletionOrder.verify(commentRepository)
                .deleteAllByReviewId(reviewId);
        deletionOrder.verify(notificationRepository)
                .deleteAllByReviewId(reviewId);
        deletionOrder.verify(reviewLikeRepository)
                .deleteAllByReviewId(reviewId);
        deletionOrder.verify(reviewRankingRepository)
                .deleteAllByReviewId(reviewId);
        deletionOrder.verify(reviewRepository)
                .delete(review);
    }

    @Test
    void 리뷰가_존재하지_않으면_물리_삭제에_실패한다() {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewRepository.findById(reviewId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.hardDelete(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewNotFoundException.class);

        verify(commentRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(notificationRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewLikeRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewRankingRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewRepository, never())
                .delete(any(Review.class));
    }

    @Test
    void 다른_사용자의_리뷰는_물리_삭제할_수_없다() {
        UUID reviewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User author = createUser(authorId);
        Book book = createBook(bookId);
        Review review = createReview(
                reviewId,
                author,
                book
        );

        given(reviewRepository.findById(reviewId))
                .willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.hardDelete(
                reviewId,
                requesterId
        )).isInstanceOf(ReviewAccessDeniedException.class);

        verify(commentRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(notificationRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewLikeRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewRankingRepository, never())
                .deleteAllByReviewId(any(UUID.class));
        verify(reviewRepository, never())
                .delete(any(Review.class));
    }

    @Test
    void 본인이_자기_리뷰에_좋아요를_추가하면_알림을_생성하지_않는다() {
        UUID requesterId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        User requester = createUser(requesterId);
        Book book = createBook(bookId);
        Review review = createReview(
                reviewId,
                requester,
                book
        );

        given(reviewRepository.findByIdForUpdate(reviewId))
                .willReturn(Optional.of(review));

        given(userRepository.findByIdAndDeletedAtIsNull(requesterId))
                .willReturn(Optional.of(requester));

        given(reviewLikeRepository.findByReviewIdAndUserId(
                reviewId,
                requesterId
        )).willReturn(Optional.empty());

        ReviewLikeResponse response = reviewService.toggleLike(
                reviewId,
                requesterId
        );

        assertThat(response.liked()).isTrue();
        assertThat(review.getLikeCount()).isEqualTo(1);

        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verifyNoInteractions(notificationService);
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