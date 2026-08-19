package com.deokhugam.book.repository;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.entity.Book;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

  private final EntityManager entityManager;

  @Override
  public List<BookSearchResult> findAllByCursor(BookSearchRequest request) {

    StringBuilder jpql = new StringBuilder(
        "SELECT b FROM Book b WHERE b.deletedAt IS NULL"
    );

    boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

    if (hasKeyword) {
      jpql.append(" AND (")
          .append("LOWER(b.title) LIKE LOWER(:keyword)")
          .append(" OR LOWER(b.author) LIKE LOWER(:keyword)")
          .append(" OR b.isbn LIKE :keyword")
          .append(")");
    }

    String sortField = switch (request.orderBy()) {
      case "publishedDate" -> "b.publishedDate";
      case "title" -> "b.title";
      default -> "b.title";
    };

    String direction = "desc".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";

    boolean hasCursor = request.cursor() != null && !request.cursor().isBlank();

    String operator = "DESC".equals(direction) ? "<" : ">";

    if (hasCursor) {
      jpql.append(" AND (")
          .append(sortField)
          .append(" ")
          .append(operator)
          .append(" :cursor");

      if (request.after() != null) {
        jpql.append(" OR (")
            .append(sortField)
            .append(" = :cursor AND b.createdAt ")
            .append(operator)
            .append(" :after)");
      }

      jpql.append(")");
    }

    jpql.append(" ORDER BY ")
        .append(sortField)
        .append(" ")
        .append(direction)
        .append(", b.createdAt ")
        .append(direction);

    var query = entityManager.createQuery(jpql.toString(), Book.class);

    if (hasKeyword) {
      query.setParameter("keyword", "%" + request.keyword() + "%");
    }

    if (hasCursor) {
      Object cursorValue = switch (request.orderBy()) {
        case "publishedDate" -> LocalDate.parse(request.cursor());
        case "title" -> request.cursor();
        default -> request.cursor();
      };

      query.setParameter("cursor", cursorValue);

      if (request.after() != null) {
        query.setParameter("after", request.after());
      }
    }

    if (request.limit() > 0) {
      query.setMaxResults(request.limit() + 1);
    }

    List<Book> books = query.getResultList();

    return books.stream()
        .map(book -> new BookSearchResult(book, 0L, 0.0))
        .toList();
  }

  @Override
  public long countAll(BookSearchRequest request) {

    StringBuilder jpql = new StringBuilder(
        "SELECT COUNT(b) FROM Book b WHERE b.deletedAt IS NULL"
    );

    boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

    if (hasKeyword) {
      jpql.append(" AND (")
          .append("LOWER(b.title) LIKE LOWER(:keyword)")
          .append(" OR LOWER(b.author) LIKE LOWER(:keyword)")
          .append(" OR b.isbn LIKE :keyword")
          .append(")");
    }

    var query = entityManager.createQuery(jpql.toString(), Long.class);

    if (hasKeyword) {
      query.setParameter(
          "keyword",
          "%" + request.keyword() + "%"
      );
    }

    return query.getSingleResult();
  }
}
