package com.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService =
                new CommentServiceImpl(
                        commentRepository,
                        userRepository
                );
    }

    @Test
    @DisplayName("댓글을 등록하면 작성자 닉네임이 포함된 응답을 반환한다")
    void createComment() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        User user = mock(User.class);

        when(user.getNickname())
                .thenReturn("테스트유저");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        // when
        CommentResponse response =
                commentService.create(request);

        // then
        assertThat(response.userId())
                .isEqualTo(userId);

        assertThat(response.reviewId())
                .isEqualTo(reviewId);

        assertThat(response.content())
                .isEqualTo("테스트 댓글입니다.");

        assertThat(response.userNickname())
                .isEqualTo("테스트유저");

        verify(commentRepository)
                .save(any(Comment.class));

        verify(userRepository)
                .findById(userId);
    }

    @Test
    @DisplayName("본인이 작성한 댓글을 수정할 수 있다")
    void updateComment() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "수정 전 댓글",
                        userId,
                        reviewId
                );

        User user = mock(User.class);

        when(user.getNickname())
                .thenReturn("작성자");

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정된 댓글"
                );

        // when
        CommentResponse response =
                commentService.update(
                        commentId,
                        userId,
                        request
                );

        // then
        assertThat(response.content())
                .isEqualTo("수정된 댓글");

        assertThat(response.userNickname())
                .isEqualTo("작성자");

        assertThat(comment.getContent())
                .isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 수정할 수 없다")
    void updateCommentByOtherUser() {

        // given
        UUID commentId = UUID.randomUUID();

        UUID writerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "댓글 내용",
                        writerId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        // when & then
        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        otherUserId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ACCESS_DENIED
                    );
                });
    }

    @Test
    @DisplayName("댓글을 논리 삭제할 수 있다")
    void softDeleteComment() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제할 댓글",
                        userId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        // when
        commentService.delete(
                commentId,
                userId
        );

        // then
        assertThat(comment.isDeleted())
                .isTrue();

        assertThat(comment.getDeletedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 삭제할 수 없다")
    void deleteCommentByOtherUser() {

        // given
        UUID commentId = UUID.randomUUID();

        UUID writerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "댓글 내용",
                        writerId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        otherUserId
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ACCESS_DENIED
                    );
                });

        assertThat(comment.isDeleted())
                .isFalse();
    }

    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다")
    void updateDeletedComment() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제된 댓글",
                        userId,
                        reviewId
                );

        comment.softDelete();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        // when & then
        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ALREADY_DELETED
                    );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외가 발생한다")
    void updateCommentNotFound() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        // when & then
        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
    void deleteCommentNotFound() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        userId
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("다음 페이지가 있으면 댓글 ID와 생성 시간을 다음 커서로 반환한다")
    void findAllWithNextPage() {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        2
                );

        Comment first =
                new Comment(
                        "첫 번째",
                        userId,
                        reviewId
                );

        Comment second =
                new Comment(
                        "두 번째",
                        userId,
                        reviewId
                );

        Comment third =
                new Comment(
                        "세 번째",
                        userId,
                        reviewId
                );

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        UUID thirdId = UUID.randomUUID();

        LocalDateTime firstCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        12,
                        0
                );

        LocalDateTime secondCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        11,
                        0
                );

        LocalDateTime thirdCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        10,
                        0
                );

        ReflectionTestUtils.setField(
                first,
                "id",
                firstId
        );

        ReflectionTestUtils.setField(
                second,
                "id",
                secondId
        );

        ReflectionTestUtils.setField(
                third,
                "id",
                thirdId
        );

        ReflectionTestUtils.setField(
                first,
                "createdAt",
                firstCreatedAt
        );

        ReflectionTestUtils.setField(
                second,
                "createdAt",
                secondCreatedAt
        );

        ReflectionTestUtils.setField(
                third,
                "createdAt",
                thirdCreatedAt
        );

        User user = mock(User.class);

        when(user.getNickname())
                .thenReturn("작성자");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(commentRepository.findAllByCursor(request))
                .thenReturn(
                        List.of(
                                first,
                                second,
                                third
                        )
                );

        when(commentRepository.countAll(request))
                .thenReturn(3L);

        // when
        CommentListResponse response =
                commentService.findAll(request);

        // then
        assertThat(response.content())
                .hasSize(2);

        assertThat(response.content().get(0).content())
                .isEqualTo("첫 번째");

        assertThat(response.content().get(1).content())
                .isEqualTo("두 번째");

        assertThat(response.content().get(0).userNickname())
                .isEqualTo("작성자");

        assertThat(response.size())
                .isEqualTo(2);

        assertThat(response.totalElements())
                .isEqualTo(3L);

        assertThat(response.hasNext())
                .isTrue();

        /*
         * 복합 커서
         *
         * cursor = 마지막 반환 댓글 ID
         * after  = 마지막 반환 댓글 createdAt
         */
        assertThat(response.nextCursor())
                .isEqualTo(secondId.toString());

        assertThat(response.nextAfter())
                .isEqualTo(secondCreatedAt);
    }

    @Test
    @DisplayName("다음 페이지가 없으면 nextCursor와 nextAfter는 null이다")
    void findAllWithoutNextPage() {

        // given
        UUID reviewId = UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        when(commentRepository.findAllByCursor(request))
                .thenReturn(List.of());

        when(commentRepository.countAll(request))
                .thenReturn(0L);

        // when
        CommentListResponse response =
                commentService.findAll(request);

        // then
        assertThat(response.content())
                .isEmpty();

        assertThat(response.size())
                .isZero();

        assertThat(response.totalElements())
                .isZero();

        assertThat(response.hasNext())
                .isFalse();

        assertThat(response.nextCursor())
                .isNull();

        assertThat(response.nextAfter())
                .isNull();
    }

    @Test
    @DisplayName("작성자를 찾지 못해도 댓글 응답의 닉네임은 빈 문자열이다")
    void responseWhenUserNotFound() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "댓글"
                );

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when
        CommentResponse response =
                commentService.create(request);

        // then
        assertThat(response.userNickname())
                .isEmpty();
    }
}