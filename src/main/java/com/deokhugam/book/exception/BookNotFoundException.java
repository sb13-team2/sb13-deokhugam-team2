package com.deokhugam.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BookNotFoundException extends DeokhugamException {

  public BookNotFoundException(UUID bookId) {
    super(
        ErrorCode.BOOK_NOT_FOUND,
        Map.of("bookId", bookId)
    );
  }
}
