package com.deokhugam.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.entity.Comment;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@Import(CommentRepositoryTest.JpaAuditingTestConfig.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingTestConfig {
    }

    @Test
    @DisplayName("댓글을 물리 삭제하면 DB에서 실제로 제거된다")
    void hardDeleteComment() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment = new Comment(
                "물리 삭제 테스트 댓글",
                userId,
                reviewId
        );

        Comment savedComment =
                commentRepository.saveAndFlush(comment);

        UUID commentId = savedComment.getId();

        // when
        commentRepository.delete(savedComment);
        commentRepository.flush();

        entityManager.clear();

        // then
        assertThat(
                commentRepository.findById(commentId)
        ).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 목록 조회에서 제외된다")
    void softDeletedCommentIsExcludedFromList() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment activeComment = new Comment(
                "정상 댓글",
                userId,
                reviewId
        );

        Comment deletedComment = new Comment(
                "삭제된 댓글",
                userId,
                reviewId
        );

        deletedComment.softDelete();

        commentRepository.save(activeComment);
        commentRepository.save(deletedComment);
        commentRepository.flush();

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        // when
        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        // then
        assertThat(comments)
                .hasSize(1);

        assertThat(comments.get(0).getContent())
                .isEqualTo("정상 댓글");
    }

    @Test
    @DisplayName("댓글 목록은 최신순으로 조회된다")
    void commentsAreSortedByCreatedAtDesc() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment firstComment = new Comment(
                "첫 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(firstComment);

        Thread.sleep(100);

        Comment secondComment = new Comment(
                "두 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(secondComment);

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        // when
        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        // then
        assertThat(comments)
                .hasSize(2);

        assertThat(comments.get(0).getContent())
                .isEqualTo("두 번째 댓글");

        assertThat(comments.get(1).getContent())
                .isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("ASC 방향으로 댓글을 조회할 수 있다")
    void commentsAreSortedByCreatedAtAsc() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment firstComment = new Comment(
                "첫 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(firstComment);

        Thread.sleep(100);

        Comment secondComment = new Comment(
                "두 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(secondComment);

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "ASC",
                        null,
                        null,
                        10
                );

        // when
        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        // then
        assertThat(comments)
                .hasSize(2);

        assertThat(comments.get(0).getContent())
                .isEqualTo("첫 번째 댓글");

        assertThat(comments.get(1).getContent())
                .isEqualTo("두 번째 댓글");
    }

    @Test
    @DisplayName("DESC 복합 커서는 createdAt이 같을 때 id를 기준으로 다음 댓글을 조회한다")
    void findCommentsByCompositeCursorDesc() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment1 = new Comment(
                "댓글 1",
                userId,
                reviewId
        );

        Comment comment2 = new Comment(
                "댓글 2",
                userId,
                reviewId
        );

        Comment comment3 = new Comment(
                "댓글 3",
                userId,
                reviewId
        );

        commentRepository.saveAll(
                List.of(
                        comment1,
                        comment2,
                        comment3
                )
        );

        commentRepository.flush();

        LocalDateTime sameCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        10,
                        0
                );

        /*
         * 세 댓글의 생성 시간을 동일하게 만들어
         * id가 실제 tie-breaker로 사용되도록 한다.
         */
        entityManager.createQuery(
                        """
                        UPDATE Comment c
                        SET c.createdAt = :createdAt,
                            c.updatedAt = :updatedAt
                        WHERE c.reviewId = :reviewId
                        """
                )
                .setParameter(
                        "createdAt",
                        sameCreatedAt
                )
                .setParameter(
                        "updatedAt",
                        sameCreatedAt
                )
                .setParameter(
                        "reviewId",
                        reviewId
                )
                .executeUpdate();

        entityManager.clear();

        /*
         * 먼저 DB가 실제로 정렬한 DESC 결과를 가져온다.
         */
        CommentSearchRequest firstPageRequest =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        List<Comment> sortedComments =
                commentRepository.findAllByCursor(
                        firstPageRequest
                );

        assertThat(sortedComments)
                .hasSize(3);

        Comment cursorComment =
                sortedComments.get(1);

        Comment expectedNextComment =
                sortedComments.get(2);

        /*
         * 두 번째 댓글을 이전 페이지의 마지막 요소라고 가정한다.
         */
        CommentSearchRequest nextPageRequest =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        cursorComment.getId().toString(),
                        cursorComment.getCreatedAt(),
                        10
                );

        // when
        List<Comment> nextComments =
                commentRepository.findAllByCursor(
                        nextPageRequest
                );

        // then
        assertThat(nextComments)
                .hasSize(1);

        assertThat(nextComments.get(0).getId())
                .isEqualTo(
                        expectedNextComment.getId()
                );
    }

    @Test
    @DisplayName("ASC 복합 커서는 createdAt이 같을 때 id를 기준으로 다음 댓글을 조회한다")
    void findCommentsByCompositeCursorAsc() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment1 = new Comment(
                "댓글 1",
                userId,
                reviewId
        );

        Comment comment2 = new Comment(
                "댓글 2",
                userId,
                reviewId
        );

        Comment comment3 = new Comment(
                "댓글 3",
                userId,
                reviewId
        );

        commentRepository.saveAll(
                List.of(
                        comment1,
                        comment2,
                        comment3
                )
        );

        commentRepository.flush();

        LocalDateTime sameCreatedAt =
                LocalDateTime.of(
                        2026,
                        8,
                        24,
                        10,
                        0
                );

        entityManager.createQuery(
                        """
                        UPDATE Comment c
                        SET c.createdAt = :createdAt,
                            c.updatedAt = :updatedAt
                        WHERE c.reviewId = :reviewId
                        """
                )
                .setParameter(
                        "createdAt",
                        sameCreatedAt
                )
                .setParameter(
                        "updatedAt",
                        sameCreatedAt
                )
                .setParameter(
                        "reviewId",
                        reviewId
                )
                .executeUpdate();

        entityManager.clear();

        /*
         * DB가 실제로 정렬한 ASC 결과를 가져온다.
         */
        CommentSearchRequest firstPageRequest =
                new CommentSearchRequest(
                        reviewId,
                        "ASC",
                        null,
                        null,
                        10
                );

        List<Comment> sortedComments =
                commentRepository.findAllByCursor(
                        firstPageRequest
                );

        assertThat(sortedComments)
                .hasSize(3);

        Comment cursorComment =
                sortedComments.get(1);

        Comment expectedNextComment =
                sortedComments.get(2);

        CommentSearchRequest nextPageRequest =
                new CommentSearchRequest(
                        reviewId,
                        "ASC",
                        cursorComment.getId().toString(),
                        cursorComment.getCreatedAt(),
                        10
                );

        // when
        List<Comment> nextComments =
                commentRepository.findAllByCursor(
                        nextPageRequest
                );

        // then
        assertThat(nextComments)
                .hasSize(1);

        assertThat(nextComments.get(0).getId())
                .isEqualTo(
                        expectedNextComment.getId()
                );
    }

    @Test
    @DisplayName("DESC 커서에서는 이전 페이지보다 오래된 댓글을 조회한다")
    void findOlderCommentWithDescCursor() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment olderComment = new Comment(
                "오래된 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(olderComment);

        Thread.sleep(100);

        Comment cursorComment = new Comment(
                "커서 댓글",
                userId,
                reviewId
        );

        Comment savedCursorComment =
                commentRepository.saveAndFlush(cursorComment);

        UUID cursorId =
                savedCursorComment.getId();

        entityManager.clear();

        Comment reloadedCursor =
                commentRepository
                        .findById(cursorId)
                        .orElseThrow();

        LocalDateTime cursorCreatedAt =
                reloadedCursor.getCreatedAt();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        cursorId.toString(),
                        cursorCreatedAt,
                        10
                );

        // when
        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        // then
        assertThat(comments)
                .extracting(Comment::getContent)
                .containsExactly("오래된 댓글");
    }

    @Test
    @DisplayName("limit + 1개의 댓글을 조회해 다음 페이지 존재 여부를 판단할 수 있다")
    void findLimitPlusOneComments() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        for (int i = 1; i <= 5; i++) {

            Comment comment = new Comment(
                    "댓글 " + i,
                    userId,
                    reviewId
            );

            commentRepository.save(comment);
        }

        commentRepository.flush();

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        3
                );

        // when
        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        // then
        assertThat(comments)
                .hasSize(4);
    }

    @Test
    @DisplayName("특정 리뷰의 삭제되지 않은 댓글 개수를 조회한다")
    void countAllComments() {

        // given
        UUID userId = UUID.randomUUID();

        UUID reviewId =
                UUID.randomUUID();

        UUID otherReviewId =
                UUID.randomUUID();

        commentRepository.save(
                new Comment(
                        "댓글 1",
                        userId,
                        reviewId
                )
        );

        commentRepository.save(
                new Comment(
                        "댓글 2",
                        userId,
                        reviewId
                )
        );

        Comment deletedComment =
                new Comment(
                        "삭제된 댓글",
                        userId,
                        reviewId
                );

        deletedComment.softDelete();

        commentRepository.save(
                deletedComment
        );

        commentRepository.save(
                new Comment(
                        "다른 리뷰의 댓글",
                        userId,
                        otherReviewId
                )
        );

        commentRepository.flush();

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        50
                );

        // when
        long count =
                commentRepository.countAll(request);

        // then
        assertThat(count)
                .isEqualTo(2L);
    }
}