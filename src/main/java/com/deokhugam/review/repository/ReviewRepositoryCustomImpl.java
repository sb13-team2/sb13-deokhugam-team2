package com.deokhugam.review.repository;

import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryCustomImpl
        implements ReviewRepositoryCustom {

    private final EntityManager entityManager;

    @Override
    public List<Review> findAllByCursor(
            ReviewSearchRequest request
    ) {
        StringBuilder jpql = new StringBuilder(
                """
                SELECT r
                FROM Review r
                JOIN FETCH r.user u
                JOIN FETCH r.book b
                WHERE r.deletedAt IS NULL
                """
        );

        appendSearchConditions(jpql, request);

        String sortField = resolveSortField(request.orderBy());
        String direction = resolveDirection(request.direction());
        String operator = direction.equals("DESC") ? "<" : ">";

        boolean hasCursor =
                request.cursor() != null
                        && !request.cursor().isBlank();

        if (hasCursor) {
            jpql.append(" AND (")
                    .append(sortField)
                    .append(" ")
                    .append(operator)
                    .append(" :cursor");

            if (request.orderBy().equals("rating")
                    && request.after() != null) {
                jpql.append(" OR (")
                        .append(sortField)
                        .append(" = :cursor")
                        .append(" AND r.createdAt ")
                        .append(operator)
                        .append(" :after)");
            }

            jpql.append(")");
        }

        appendOrderBy(
                jpql,
                request.orderBy(),
                direction
        );

        TypedQuery<Review> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Review.class
                );

        setSearchParameters(query, request);

        if (hasCursor) {
            query.setParameter(
                    "cursor",
                    parseCursor(
                            request.orderBy(),
                            request.cursor()
                    )
            );

            if (request.orderBy().equals("rating")
                    && request.after() != null) {
                query.setParameter(
                        "after",
                        request.after()
                );
            }
        }

        query.setMaxResults(request.limit() + 1);

        return query.getResultList();
    }

    @Override
    public long countAll(
            ReviewSearchRequest request
    ) {
        StringBuilder jpql = new StringBuilder(
                """
                SELECT COUNT(r)
                FROM Review r
                JOIN r.user u
                JOIN r.book b
                WHERE r.deletedAt IS NULL
                """
        );

        appendSearchConditions(jpql, request);

        TypedQuery<Long> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Long.class
                );

        setSearchParameters(query, request);

        return query.getSingleResult();
    }

    private void appendSearchConditions(
            StringBuilder jpql,
            ReviewSearchRequest request
    ) {
        if (request.userId() != null) {
            jpql.append(" AND u.id = :userId");
        }

        if (request.bookId() != null) {
            jpql.append(" AND b.id = :bookId");
        }

        if (request.keyword() != null
                && !request.keyword().isBlank()) {
            jpql.append(
                    """
                     AND (
                         LOWER(u.nickname) LIKE LOWER(:keyword)
                         OR LOWER(r.content) LIKE LOWER(:keyword)
                         OR LOWER(b.title) LIKE LOWER(:keyword)
                     )
                    """
            );
        }
    }

    private void setSearchParameters(
            TypedQuery<?> query,
            ReviewSearchRequest request
    ) {
        if (request.userId() != null) {
            query.setParameter(
                    "userId",
                    request.userId()
            );
        }

        if (request.bookId() != null) {
            query.setParameter(
                    "bookId",
                    request.bookId()
            );
        }

        if (request.keyword() != null
                && !request.keyword().isBlank()) {
            query.setParameter(
                    "keyword",
                    "%" + request.keyword() + "%"
            );
        }
    }

    private String resolveSortField(String orderBy) {
        return switch (orderBy) {
            case "rating" -> "r.rating";
            case "createdAt" -> "r.createdAt";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 정렬 기준입니다: " + orderBy
            );
        };
    }

    private String resolveDirection(String direction) {
        if (direction.equalsIgnoreCase("ASC")) {
            return "ASC";
        }

        if (direction.equalsIgnoreCase("DESC")) {
            return "DESC";
        }

        throw new IllegalArgumentException(
                "지원하지 않는 정렬 방향입니다: " + direction
        );
    }

    private Object parseCursor(
            String orderBy,
            String cursor
    ) {
        return switch (orderBy) {
            case "rating" -> Integer.parseInt(cursor);
            case "createdAt" -> LocalDateTime.parse(cursor);
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 커서 형식입니다."
            );
        };
    }

    private void appendOrderBy(
            StringBuilder jpql,
            String orderBy,
            String direction
    ) {
        if (orderBy.equals("rating")) {
            jpql.append(" ORDER BY r.rating ")
                    .append(direction)
                    .append(", r.createdAt ")
                    .append(direction)
                    .append(", r.id ")
                    .append(direction);
            return;
        }

        jpql.append(" ORDER BY r.createdAt ")
                .append(direction)
                .append(", r.id ")
                .append(direction);
    }
}