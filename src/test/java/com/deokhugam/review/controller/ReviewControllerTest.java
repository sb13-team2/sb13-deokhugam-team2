package com.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.global.config.SecurityConfig;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListItemResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;
import com.deokhugam.review.exception.DuplicateReviewException;
import com.deokhugam.review.exception.ReviewAccessDeniedException;
import com.deokhugam.review.exception.ReviewNotFoundException;
import com.deokhugam.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
class ReviewControllerTest {

    private static final String REQUEST_USER_ID_HEADER =
            "Deokhugam-Request-User-ID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void 리뷰를_등록하면_201과_등록된_리뷰를_반환한다() throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        ReviewDetailResponse response = new ReviewDetailResponse(
                reviewId,
                bookId,
                "테스트 도서",
                "thumbnail.jpg",
                userId,
                "리아",
                "좋은 책입니다.",
                5,
                0,
                0,
                false,
                now,
                now
        );

        given(reviewService.create(any(ReviewCreateRequest.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.content").value("좋은 책입니다."))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.likedByMe").value(false));
    }

    @Test
    void 평점이_범위를_벗어나면_400을_반환한다() throws Exception {
        ReviewCreateRequest request = new ReviewCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "좋은 책입니다.",
                0
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_INPUT_VALUE"));

        verify(reviewService, never())
                .create(any(ReviewCreateRequest.class));
    }

    @Test
    void 활성_리뷰가_이미_존재하면_409를_반환한다() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewCreateRequest request = new ReviewCreateRequest(
                bookId,
                userId,
                "좋은 책입니다.",
                5
        );

        given(reviewService.create(any(ReviewCreateRequest.class)))
                .willThrow(new DuplicateReviewException(userId, bookId));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_REVIEW"))
                .andExpect(jsonPath("$.details.userId")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.details.bookId")
                        .value(bookId.toString()));
    }

    @Test
    void 본인의_리뷰를_수정하면_200과_수정된_리뷰를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        ReviewDetailResponse response = new ReviewDetailResponse(
                reviewId,
                bookId,
                "테스트 도서",
                "thumbnail.jpg",
                userId,
                "리아",
                "수정한 리뷰 내용입니다.",
                4,
                0,
                0,
                false,
                now,
                now
        );

        given(reviewService.update(
                eq(reviewId),
                eq(userId),
                any(ReviewUpdateRequest.class)
        )).willReturn(response);

        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .header(REQUEST_USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.content")
                        .value("수정한 리뷰 내용입니다."))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void 리뷰_수정_평점이_범위를_벗어나면_400을_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                0
        );

        mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                        .header(REQUEST_USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_INPUT_VALUE"));

        verify(reviewService, never()).update(
                any(UUID.class),
                any(UUID.class),
                any(ReviewUpdateRequest.class)
        );
    }

    @Test
    void 리뷰_ID로_상세_정보를_조회하면_200과_리뷰를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReviewDetailResponse response = new ReviewDetailResponse(
                reviewId,
                bookId,
                "테스트 도서",
                "https://example.com/thumbnail.jpg",
                authorId,
                "작성자",
                "좋은 책입니다.",
                5,
                3,
                2,
                true,
                now,
                now
        );

        given(reviewService.findById(
                eq(reviewId),
                eq(requesterId)
        )).willReturn(response);

        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.bookId")
                        .value(bookId.toString()))
                .andExpect(jsonPath("$.bookTitle")
                        .value("테스트 도서"))
                .andExpect(jsonPath("$.bookThumbnailUrl")
                        .value("https://example.com/thumbnail.jpg"))
                .andExpect(jsonPath("$.userId")
                        .value(authorId.toString()))
                .andExpect(jsonPath("$.userNickname")
                        .value("작성자"))
                .andExpect(jsonPath("$.content")
                        .value("좋은 책입니다."))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.likeCount").value(3))
                .andExpect(jsonPath("$.commentCount").value(2))
                .andExpect(jsonPath("$.likedByMe").value(true));
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_상세_조회시_404를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewService.findById(
                eq(reviewId),
                eq(requesterId)
        )).willThrow(new ReviewNotFoundException(reviewId));

        mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("REVIEW_NOT_FOUND"))
                .andExpect(jsonPath("$.details.reviewId")
                        .value(reviewId.toString()));
    }

    @Test
    void 본인의_리뷰를_논리_삭제하면_204를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isNoContent());

        verify(reviewService).softDelete(
                reviewId,
                requesterId
        );
    }

    @Test
    void 다른_사용자의_리뷰를_논리_삭제하면_403을_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        willThrow(
                new ReviewAccessDeniedException(
                        reviewId,
                        requesterId
                )
        ).given(reviewService).softDelete(
                reviewId,
                requesterId
        );

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code")
                        .value("REVIEW_ACCESS_DENIED"))
                .andExpect(jsonPath("$.details.reviewId")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.details.requesterId")
                        .value(requesterId.toString()))
                .andExpect(jsonPath("$.message")
                        .value("리뷰에 대한 권한이 없습니다."));
    }

    @Test
    void 리뷰_좋아요를_토글하면_200과_좋아요_상태를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        ReviewLikeResponse response = new ReviewLikeResponse(
                reviewId,
                requesterId,
                true
        );

        given(reviewService.toggleLike(
                eq(reviewId),
                eq(requesterId)
        )).willReturn(response);

        mockMvc.perform(post(
                        "/api/reviews/{reviewId}/like",
                        reviewId
                )
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.userId")
                        .value(requesterId.toString()))
                .andExpect(jsonPath("$.liked").value(true));

        verify(reviewService).toggleLike(
                reviewId,
                requesterId
        );
    }

    @Test
    void 활성_리뷰가_존재하지_않으면_좋아요시_404를_반환한다()
            throws Exception {
        UUID reviewId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        given(reviewService.toggleLike(
                eq(reviewId),
                eq(requesterId)
        )).willThrow(new ReviewNotFoundException(reviewId));

        mockMvc.perform(post(
                        "/api/reviews/{reviewId}/like",
                        reviewId
                )
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("REVIEW_NOT_FOUND"))
                .andExpect(jsonPath("$.details.reviewId")
                        .value(reviewId.toString()));
    }

    @Test
    void 리뷰_목록을_조회하면_200과_커서_페이지를_반환한다()
            throws Exception {
        UUID requesterId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        LocalDateTime createdAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);

        ReviewSearchRequest request = new ReviewSearchRequest(
                authorId,
                bookId,
                "좋은",
                "rating",
                "DESC",
                "5",
                createdAt,
                10
        );

        ReviewListItemResponse review = new ReviewListItemResponse(
                reviewId,
                bookId,
                "테스트 도서",
                "https://example.com/thumbnail.jpg",
                authorId,
                "작성자",
                "좋은 책입니다.",
                4,
                3,
                2,
                true,
                createdAt,
                createdAt
        );

        ReviewListResponse response = new ReviewListResponse(
                List.of(review),
                "4",
                createdAt,
                1,
                2L,
                true
        );

        given(reviewService.findAll(
                eq(request),
                eq(requesterId)
        )).willReturn(response);

        mockMvc.perform(get("/api/reviews")
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        )
                        .param("userId", authorId.toString())
                        .param("bookId", bookId.toString())
                        .param("keyword", "좋은")
                        .param("orderBy", "rating")
                        .param("direction", "DESC")
                        .param("cursor", "5")
                        .param("after", createdAt.toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(reviewId.toString()))
                .andExpect(jsonPath("$.content[0].bookId")
                        .value(bookId.toString()))
                .andExpect(jsonPath("$.content[0].bookTitle")
                        .value("테스트 도서"))
                .andExpect(jsonPath("$.content[0].userId")
                        .value(authorId.toString()))
                .andExpect(jsonPath("$.content[0].content")
                        .value("좋은 책입니다."))
                .andExpect(jsonPath("$.content[0].rating").value(4))
                .andExpect(jsonPath("$.content[0].likedByMe")
                        .value(true))
                .andExpect(jsonPath("$.nextCursor").value("4"))
                .andExpect(jsonPath("$.nextAfter")
                        .value("2026-08-21T10:00:00"))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(reviewService).findAll(
                request,
                requesterId
        );
    }

    @Test
    void 리뷰_목록의_정렬_기준이_올바르지_않으면_400을_반환한다()
            throws Exception {
        UUID requesterId = UUID.randomUUID();

        mockMvc.perform(get("/api/reviews")
                        .header(
                                REQUEST_USER_ID_HEADER,
                                requesterId
                        )
                        .param("orderBy", "likeCount"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_INPUT_VALUE"));

        verify(reviewService, never()).findAll(
                any(ReviewSearchRequest.class),
                any(UUID.class)
        );
    }
}
