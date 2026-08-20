package com.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.global.config.SecurityConfig;
import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.exception.DuplicateReviewException;
import com.deokhugam.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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
}