package com.deokhugam.comment.repository;

import com.deokhugam.comment.entity.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository
        extends JpaRepository<Comment, UUID>,
        CommentRepositoryCustom {

    void deleteAllByReviewId(UUID reviewId);

    void deleteAllByUserId(UUID userId);

    List<Comment> findAllByUserId(UUID userId);

    @Modifying
    @Query(
            value = """
                    UPDATE reviews
                    SET comment_count = comment_count + 1
                    WHERE id = :reviewId
                    """,
            nativeQuery = true
    )
    void increaseReviewCommentCount(
            @Param("reviewId") UUID reviewId
    );

    @Modifying
    @Query(
            value = """
                    UPDATE reviews
                    SET comment_count =
                        CASE
                            WHEN comment_count > 0
                            THEN comment_count - 1
                            ELSE 0
                        END
                    WHERE id = :reviewId
                    """,
            nativeQuery = true
    )
    void decreaseReviewCommentCount(
            @Param("reviewId") UUID reviewId
    );
}