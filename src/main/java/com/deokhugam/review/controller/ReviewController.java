package com.deokhugam.review.controller;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final String REQUEST_USER_ID_HEADER =
            "Deokhugam-Request-User-ID";

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDetailResponse> create(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResponse response = reviewService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

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

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> update(
            @PathVariable UUID reviewId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewDetailResponse response = reviewService.update(
                reviewId,
                requesterId,
                request
        );

        return ResponseEntity.ok(response);
    }

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
}