package com.deokhugam.comment.repository;

import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.entity.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl
        implements CommentRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Comment> findAllByCursor(
            CommentSearchRequest request
    ) {
        StringBuilder jpql = new StringBuilder(
                """
                SELECT c
                FROM Comment c
                WHERE c.deletedAt IS NULL
                  AND c.reviewId = :reviewId
                """
        );

        String direction =
                resolveDirection(request.direction());

        String operator =
                direction.equals("DESC") ? "<" : ">";

        boolean hasCursor =
                request.cursor() != null
                        && !request.cursor().isBlank()
                        && request.after() != null;

        /*
         * createdAt이 같은 댓글이 여러 개 존재할 수 있으므로
         * createdAt + id를 복합 커서로 사용한다.
         *
         * DESC:
         * createdAt < after
         * 또는
         * createdAt = after && id < cursor
         *
         * ASC는 반대.
         */
        if (hasCursor) {
            jpql.append(" AND (")
                    .append("c.createdAt ")
                    .append(operator)
                    .append(" :after")
                    .append(" OR (c.createdAt = :after")
                    .append(" AND c.id ")
                    .append(operator)
                    .append(" :cursor)")
                    .append(")");
        }

        jpql.append(" ORDER BY c.createdAt ")
                .append(direction)
                .append(", c.id ")
                .append(direction);

        TypedQuery<Comment> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Comment.class
                );

        query.setParameter(
                "reviewId",
                request.reviewId()
        );

        if (hasCursor) {
            query.setParameter(
                    "after",
                    request.after()
            );

            query.setParameter(
                    "cursor",
                    UUID.fromString(request.cursor())
            );
        }

        query.setMaxResults(
                request.limit() + 1
        );

        return query.getResultList();
    }

    @Override
    public long countAll(
            CommentSearchRequest request
    ) {
        TypedQuery<Long> query =
                entityManager.createQuery(
                        """
                        SELECT COUNT(c)
                        FROM Comment c
                        WHERE c.deletedAt IS NULL
                          AND c.reviewId = :reviewId
                        """,
                        Long.class
                );

        query.setParameter(
                "reviewId",
                request.reviewId()
        );

        return query.getSingleResult();
    }

    private String resolveDirection(
            String direction
    ) {
        if ("ASC".equalsIgnoreCase(direction)) {
            return "ASC";
        }

        if ("DESC".equalsIgnoreCase(direction)) {
            return "DESC";
        }

        throw new IllegalArgumentException(
                "지원하지 않는 정렬 방향입니다: "
                        + direction
        );
    }
}