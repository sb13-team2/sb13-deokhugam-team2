package com.deokhugam.review.service;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.exception.DuplicateReviewException;
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
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

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

        return toResponse(savedReview);
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

    private ReviewDetailResponse toResponse(Review review) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getBook().getId(),
                review.getBook().getTitle(),
                review.getBook().getThumbnailUrl(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getContent(),
                review.getRating(),
                review.getLikeCount(),
                review.getCommentCount(),
                false,
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}