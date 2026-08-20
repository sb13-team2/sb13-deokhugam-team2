package com.deokhugam.review.service;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.exception.ErrorCode;
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
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReviewService implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final Storage storage;

    @Override
    @Transactional
    public ReviewDetailResponse create(ReviewCreateRequest request) {
        validateDuplicateReview(
                request.userId(),
                request.bookId()
        );

        User user = userRepository
                .findByIdAndDeletedAtIsNull(request.userId())
                .orElseThrow(() -> new UserException(
                        ErrorCode.USER_NOT_FOUND,
                        Map.of("userId", request.userId())
                ));

        Book book = bookRepository
                .findByIdAndDeletedAtIsNull(request.bookId())
                .orElseThrow(() ->
                        new BookNotFoundException(request.bookId())
                );

        Review review = Review.create(
                user,
                book,
                request.content(),
                request.rating()
        );

        Review savedReview = reviewRepository.save(review);

        return toResponse(savedReview, false);
    }

    @Override
    public ReviewDetailResponse findById(
            UUID reviewId,
            UUID requesterId
    ) {
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId)
                );

        boolean likedByMe =
                reviewLikeRepository.existsByReviewIdAndUserId(
                        reviewId,
                        requesterId
                );

        return toResponse(review, likedByMe);
    }

    @Override
    @Transactional
    public ReviewDetailResponse update(
            UUID reviewId,
            UUID requesterId,
            ReviewUpdateRequest request
    ) {
        Review review = reviewRepository
                .findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() ->
                        new ReviewNotFoundException(reviewId)
                );

        validateReviewOwner(review, requesterId);

        review.update(
                request.content(),
                request.rating()
        );

        Review updatedReview = reviewRepository.saveAndFlush(review);

        boolean likedByMe =
                reviewLikeRepository.existsByReviewIdAndUserId(
                        reviewId,
                        requesterId
                );

        return toResponse(updatedReview, likedByMe);
    }

    private void validateDuplicateReview(
            UUID userId,
            UUID bookId
    ) {
        boolean duplicate =
                reviewRepository
                        .existsByUserIdAndBookIdAndDeletedAtIsNull(
                                userId,
                                bookId
                        );

        if (duplicate) {
            throw new DuplicateReviewException(userId, bookId);
        }
    }

    private void validateReviewOwner(
            Review review,
            UUID requesterId
    ) {
        if (!review.getUser().getId().equals(requesterId)) {
            throw new ReviewAccessDeniedException(
                    review.getId(),
                    requesterId
            );
        }
    }

    private ReviewDetailResponse toResponse(
            Review review,
            boolean likedByMe
    ) {
        String thumbnailUrl = review.getBook().getThumbnailUrl();

        if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
            thumbnailUrl = storage.getUrl(thumbnailUrl);
        }

        return new ReviewDetailResponse(
                review.getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                thumbnailUrl,
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                likedByMe,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}