package com.deokhugam.book.dto.request;

import java.time.LocalDateTime;

public record BookSearchRequest(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    LocalDateTime after,
    int limit
) {

  public BookSearchRequest {
    if (orderBy == null || orderBy.isBlank()) {
      orderBy = "title";
    }

    if (direction == null || direction.isBlank()) {
      direction = "DESC";
    }

    if (limit <= 0) {
      limit = 50;
    }
  }
}
