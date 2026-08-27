package com.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.deokhugam.notification.entity.NotificationType;
import com.deokhugam.notification.service.NotificationService;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.exception.ReviewNotFoundException;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private NotificationService notificationService;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService =
                new CommentServiceImpl(
                        commentRepository,
                        userRepository,
                        reviewRepository,
                        notificationService
                );
    }

    @Test
    @DisplayName("활성 사용자가 다른 사용자의 리뷰에 댓글을 등록하면 알림을 생성한다")
    void createComment() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID reviewWriterId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        User commenter =
                mock(User.class);

        User reviewWriter =
                mock(User.class);

        Review review =
                mock(Review.class);

        when(commenter.getId())
                .thenReturn(userId);

        when(commenter.getNickname())
                .thenReturn("테스트유저");

        when(review.getId())
                .thenReturn(reviewId);

        when(review.getUser())
                .thenReturn(reviewWriter);

        when(reviewWriter.getId())
                .thenReturn(reviewWriterId);

        when(
                userRepository.findByIdAndDeletedAtIsNull(
                        userId
                )
        ).thenReturn(
                Optional.of(commenter)
        );

        when(
                reviewRepository.findByIdAndDeletedAtIsNull(
                        reviewId
                )
        ).thenReturn(
                Optional.of(review)
        );

        when(
                commentRepository.save(
                        any(Comment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        CommentResponse response =
                commentService.create(request);

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

        verify(notificationService)
                .createNotification(
                        reviewWriter,
                        review,
                        "회원님의 리뷰에 새로운 댓글이 등록되었습니다.",
                        NotificationType.NEW_COMMENT
                );
    }

    @Test
    @DisplayName("활성 상태가 아닌 사용자는 댓글을 등록할 수 없다")
    void createCommentWithInactiveUser() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "댓글 내용"
                );

        when(
                userRepository.findByIdAndDeletedAtIsNull(
                        userId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> commentService.create(request)
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.USER_NOT_FOUND
                            );
                });

        verify(
                commentRepository,
                never()
        ).save(any(Comment.class));

        verify(
                reviewRepository,
                never()
        ).findByIdAndDeletedAtIsNull(
                any(UUID.class)
        );

        verify(
                notificationService,
                never()
        ).createNotification(
                any(User.class),
                any(Review.class),
                anyString(),
                any(NotificationType.class)
        );
    }

    @Test
    @DisplayName("활성 상태가 아닌 리뷰에는 댓글을 등록할 수 없다")
    void createCommentWithInactiveReview() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "댓글 내용"
                );

        User commenter =
                mock(User.class);

        when(
                userRepository.findByIdAndDeletedAtIsNull(
                        userId
                )
        ).thenReturn(
                Optional.of(commenter)
        );

        when(
                reviewRepository.findByIdAndDeletedAtIsNull(
                        reviewId
                )
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> commentService.create(request)
        )
                .isInstanceOf(
                        ReviewNotFoundException.class
                );

        verify(
                commentRepository,
                never()
        ).save(any(Comment.class));

        verify(
                notificationService,
                never()
        ).createNotification(
                any(User.class),
                any(Review.class),
                anyString(),
                any(NotificationType.class)
        );
    }

    @Test
    @DisplayName("리뷰 작성자가 자신의 리뷰에 댓글을 작성하면 알림을 생성하지 않는다")
    void createCommentOnOwnReviewDoesNotCreateNotification() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "내 리뷰 댓글"
                );

        User commenter =
                mock(User.class);

        Review review =
                mock(Review.class);

        when(commenter.getId())
                .thenReturn(userId);

        when(commenter.getNickname())
                .thenReturn("작성자");

        when(review.getId())
                .thenReturn(reviewId);

        when(review.getUser())
                .thenReturn(commenter);

        when(
                userRepository.findByIdAndDeletedAtIsNull(
                        userId
                )
        ).thenReturn(
                Optional.of(commenter)
        );

        when(
                reviewRepository.findByIdAndDeletedAtIsNull(
                        reviewId
                )
        ).thenReturn(
                Optional.of(review)
        );

        when(
                commentRepository.save(
                        any(Comment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        CommentResponse response =
                commentService.create(request);

        assertThat(response.userNickname())
                .isEqualTo("작성자");

        verify(
                notificationService,
                never()
        ).createNotification(
                any(User.class),
                any(Review.class),
                anyString(),
                any(NotificationType.class)
        );
    }

    @Test
    @DisplayName("본인이 작성한 댓글을 수정할 수 있다")
    void updateComment() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "수정 전 댓글",
                        userId,
                        reviewId
                );

        User user =
                mock(User.class);

        when(user.getNickname())
                .thenReturn("작성자");

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.of(comment)
        );

        when(
                userRepository.findById(userId)
        ).thenReturn(
                Optional.of(user)
        );

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정된 댓글"
                );

        CommentResponse response =
                commentService.update(
                        commentId,
                        userId,
                        request
                );

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

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.of(comment)
        );

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        otherUserId,
                        request
                )
        )
                .isInstanceOf(
                        DeokhugamException.class
                )
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.COMMENT_ACCESS_DENIED
                            );
                });
    }

    @Test
    @DisplayName("댓글을 논리 삭제할 수 있다")
    void softDeleteComment() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제할 댓글",
                        userId,
                        reviewId
                );

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.of(comment)
        );

        commentService.delete(
                commentId,
                userId
        );

        assertThat(comment.isDeleted())
                .isTrue();

        assertThat(comment.getDeletedAt())
                .isNotNull();
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 삭제할 수 없다")
    void deleteCommentByOtherUser() {

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

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.of(comment)
        );

        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        otherUserId
                )
        )
                .isInstanceOf(
                        DeokhugamException.class
                )
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.COMMENT_ACCESS_DENIED
                            );
                });

        assertThat(comment.isDeleted())
                .isFalse();
    }

    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다")
    void updateDeletedComment() {

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

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.of(comment)
        );

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(
                        DeokhugamException.class
                )
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.COMMENT_ALREADY_DELETED
                            );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외가 발생한다")
    void updateCommentNotFound() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.empty()
        );

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(
                        DeokhugamException.class
                )
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.COMMENT_NOT_FOUND
                            );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
    void deleteCommentNotFound() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(
                commentRepository.findById(commentId)
        ).thenReturn(
                Optional.empty()
        );

        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        userId
                )
        )
                .isInstanceOf(
                        DeokhugamException.class
                )
                .satisfies(exception -> {

                    DeokhugamException e =
                            (DeokhugamException) exception;

                    assertThat(e.getErrorCode())
                            .isEqualTo(
                                    ErrorCode.COMMENT_NOT_FOUND
                            );
                });
    }

    @Test
    @DisplayName("다음 페이지가 있으면 댓글 ID와 생성 시간을 다음 커서로 반환한다")
    void findAllWithNextPage() {

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

        User user =
                mock(User.class);

        when(user.getId())
                .thenReturn(userId);

        when(user.getNickname())
                .thenReturn("작성자");

        when(
                commentRepository.findAllByCursor(request)
        ).thenReturn(
                List.of(
                        first,
                        second,
                        third
                )
        );

        /*
         * limit이 2이므로 실제 응답에 사용되는
         * first, second의 작성자를 한 번에 조회한다.
         */
        when(
                userRepository.findAllByIdInAndDeletedAtIsNull(
                        Set.of(userId)
                )
        ).thenReturn(
                List.of(user)
        );

        when(
                commentRepository.countAll(request)
        ).thenReturn(3L);

        CommentListResponse response =
                commentService.findAll(request);

        assertThat(response.content())
                .hasSize(2);

        assertThat(response.content().get(0).userNickname())
                .isEqualTo("작성자");

        assertThat(response.content().get(1).userNickname())
                .isEqualTo("작성자");

        assertThat(response.size())
                .isEqualTo(2);

        assertThat(response.totalElements())
                .isEqualTo(3L);

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(
                        secondId.toString()
                );

        assertThat(response.nextAfter())
                .isEqualTo(
                        secondCreatedAt
                );

        verify(userRepository)
                .findAllByIdInAndDeletedAtIsNull(
                        Set.of(userId)
                );

        verify(
                userRepository,
                never()
        ).findById(userId);
    }

    @Test
    @DisplayName("댓글 목록의 작성자들은 한 번의 일괄 조회로 가져온다")
    void findAllLoadsUsersInBatch() {

        UUID reviewId = UUID.randomUUID();

        UUID firstUserId =
                UUID.randomUUID();

        UUID secondUserId =
                UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        Comment first =
                new Comment(
                        "첫 번째 사용자 댓글",
                        firstUserId,
                        reviewId
                );

        Comment second =
                new Comment(
                        "두 번째 사용자 댓글",
                        secondUserId,
                        reviewId
                );

        User firstUser =
                mock(User.class);

        User secondUser =
                mock(User.class);

        when(firstUser.getId())
                .thenReturn(firstUserId);

        when(firstUser.getNickname())
                .thenReturn("사용자1");

        when(secondUser.getId())
                .thenReturn(secondUserId);

        when(secondUser.getNickname())
                .thenReturn("사용자2");

        when(
                commentRepository.findAllByCursor(request)
        ).thenReturn(
                List.of(
                        first,
                        second
                )
        );

        when(
                userRepository.findAllByIdInAndDeletedAtIsNull(
                        Set.of(
                                firstUserId,
                                secondUserId
                        )
                )
        ).thenReturn(
                List.of(
                        firstUser,
                        secondUser
                )
        );

        when(
                commentRepository.countAll(request)
        ).thenReturn(2L);

        CommentListResponse response =
                commentService.findAll(request);

        assertThat(response.content())
                .hasSize(2);

        assertThat(
                response.content()
                        .get(0)
                        .userNickname()
        ).isEqualTo("사용자1");

        assertThat(
                response.content()
                        .get(1)
                        .userNickname()
        ).isEqualTo("사용자2");

        /*
         * 작성자가 두 명이어도 Repository는
         * 한 번의 일괄 조회만 수행한다.
         */
        verify(userRepository)
                .findAllByIdInAndDeletedAtIsNull(
                        Set.of(
                                firstUserId,
                                secondUserId
                        )
                );

        /*
         * 댓글마다 findById()를 호출하지 않는다.
         */
        verify(
                userRepository,
                never()
        ).findById(any(UUID.class));
    }

    @Test
    @DisplayName("다음 페이지가 없으면 nextCursor와 nextAfter는 null이다")
    void findAllWithoutNextPage() {

        UUID reviewId =
                UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        when(
                commentRepository.findAllByCursor(request)
        ).thenReturn(
                List.of()
        );

        when(
                commentRepository.countAll(request)
        ).thenReturn(0L);

        CommentListResponse response =
                commentService.findAll(request);

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

        /*
         * 댓글 자체가 없으면 사용자 조회도 하지 않는다.
         */
        verify(
                userRepository,
                never()
        ).findAllByIdInAndDeletedAtIsNull(
                any()
        );
    }
}