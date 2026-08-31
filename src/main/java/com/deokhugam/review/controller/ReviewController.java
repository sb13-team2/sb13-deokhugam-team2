package com.deokhugam.review.controller;

import com.deokhugam.review.controller.doc.ReviewControllerDoc;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;
import com.deokhugam.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerDoc {

    private static final String REQUEST_USER_ID_HEADER =
            "Deokhugam-Request-User-ID";

    private final ReviewService reviewService;

    @Override
    @PostMapping
    public ResponseEntity<ReviewDetailResponse> create(
            @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResponse response = reviewService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ReviewListResponse> findAll(
            @Parameter(hidden = true)
            @ModelAttribute ReviewSearchRequest request,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        ReviewListResponse response = reviewService.findAll(
                request,
                requesterId
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> findById(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        ReviewDetailResponse response = reviewService.findById(
                reviewId,
                requesterId
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> update(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId,
            @RequestBody ReviewUpdateRequest request
    ) {
        ReviewDetailResponse response = reviewService.update(
                reviewId,
                requesterId,
                request
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDelete(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        reviewService.softDelete(
                reviewId,
                requesterId
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDelete(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        reviewService.hardDelete(
                reviewId,
                requesterId
        );

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeResponse> toggleLike(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        ReviewLikeResponse response = reviewService.toggleLike(
                reviewId,
                requesterId
        );

        return ResponseEntity.ok(response);
    }
}
