package com.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private static final String REQUEST_USER_ID_HEADER =
            "Deokhugam-Request-User-ID";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        CommentController commentController =
                new CommentController(commentService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("댓글을 등록하면 201 Created를 반환한다")
    void createComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        CommentResponse response =
                new CommentResponse(
                        commentId,
                        userId,
                        "테스트유저",
                        reviewId,
                        "테스트 댓글입니다.",
                        LocalDateTime.of(2026, 8, 24, 10, 0),
                        LocalDateTime.of(2026, 8, 24, 10, 0)
                );

        when(
                commentService.create(
                        any(CommentCreateRequest.class)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(commentId.toString())
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.userNickname")
                                .value("테스트유저")
                )
                .andExpect(
                        jsonPath("$.reviewId")
                                .value(reviewId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("테스트 댓글입니다.")
                );
    }

    @Test
    @DisplayName("댓글을 수정할 수 있다")
    void updateComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정된 댓글"
                );

        CommentResponse response =
                new CommentResponse(
                        commentId,
                        userId,
                        "작성자",
                        reviewId,
                        "수정된 댓글",
                        LocalDateTime.of(2026, 8, 24, 10, 0),
                        LocalDateTime.of(2026, 8, 24, 11, 0)
                );

        when(
                commentService.update(
                        eq(commentId),
                        eq(userId),
                        any(CommentUpdateRequest.class)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .header(
                                        REQUEST_USER_ID_HEADER,
                                        userId.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(commentId.toString())
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.userNickname")
                                .value("작성자")
                )
                .andExpect(
                        jsonPath("$.reviewId")
                                .value(reviewId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("수정된 댓글")
                );
    }

    @Test
    @DisplayName("댓글을 삭제하면 204 No Content를 반환한다")
    void deleteComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing()
                .when(commentService)
                .delete(commentId, userId);

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .header(
                                        REQUEST_USER_ID_HEADER,
                                        userId.toString()
                                )
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("리뷰별 댓글 목록을 조회할 수 있다")
    void findAllComments() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentResponse first =
                new CommentResponse(
                        UUID.randomUUID(),
                        userId,
                        "작성자",
                        reviewId,
                        "첫 번째 댓글",
                        LocalDateTime.of(2026, 8, 24, 11, 0),
                        LocalDateTime.of(2026, 8, 24, 11, 0)
                );

        CommentResponse second =
                new CommentResponse(
                        UUID.randomUUID(),
                        userId,
                        "작성자",
                        reviewId,
                        "두 번째 댓글",
                        LocalDateTime.of(2026, 8, 24, 10, 0),
                        LocalDateTime.of(2026, 8, 24, 10, 0)
                );

        CommentListResponse response =
                new CommentListResponse(
                        List.of(first, second),
                        null,
                        null,
                        2,
                        2L,
                        false
                );

        when(
                commentService.findAll(
                        any(CommentSearchRequest.class)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.content[0].content")
                                .value("첫 번째 댓글")
                )
                .andExpect(
                        jsonPath("$.content[0].userNickname")
                                .value("작성자")
                )
                .andExpect(
                        jsonPath("$.content[1].content")
                                .value("두 번째 댓글")
                )
                .andExpect(
                        jsonPath("$.content[1].userNickname")
                                .value("작성자")
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.totalElements")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.hasNext")
                                .value(false)
                );
    }

    @Test
    @DisplayName("복합 커서로 댓글 목록을 조회할 수 있다")
    void findAllCommentsWithCursor() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID cursorId = UUID.randomUUID();

        LocalDateTime after =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        10,
                        0
                );

        CommentListResponse response =
                new CommentListResponse(
                        List.of(),
                        null,
                        null,
                        0,
                        0L,
                        false
                );

        when(
                commentService.findAll(
                        any(CommentSearchRequest.class)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "direction",
                                        "DESC"
                                )
                                .param(
                                        "cursor",
                                        cursorId.toString()
                                )
                                .param(
                                        "after",
                                        after.toString()
                                )
                                .param(
                                        "limit",
                                        "10"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content.length()")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.size")
                                .value(0)
                )
                .andExpect(
                        jsonPath("$.hasNext")
                                .value(false)
                );
    }

    @Test
    @DisplayName("댓글 등록 시 내용이 비어 있으면 400을 반환한다")
    void createCommentWithBlankContent() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        ""
                );

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 수정 시 내용이 비어 있으면 400을 반환한다")
    void updateCommentWithBlankContent() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentUpdateRequest request =
                new CommentUpdateRequest("");

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .header(
                                        REQUEST_USER_ID_HEADER,
                                        userId.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor를 사용하면서 after가 없으면 400을 반환한다")
    void findAllCommentsWithCursorWithoutAfter() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID cursorId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "cursor",
                                        cursorId.toString()
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("cursor가 UUID 형식이 아니면 400을 반환한다")
    void findAllCommentsWithInvalidCursor() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "cursor",
                                        "invalid-cursor"
                                )
                                .param(
                                        "after",
                                        "2026-08-24T10:00:00"
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("direction이 잘못된 값이면 400을 반환한다")
    void findAllCommentsWithInvalidDirection() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "direction",
                                        "WRONG"
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("limit이 1보다 작으면 400을 반환한다")
    void findAllCommentsWithInvalidLimit() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "limit",
                                        "0"
                                )
                )
                .andExpect(status().isBadRequest());
    }
}