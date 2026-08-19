package com.deokhugam.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class DuplicateBookException extends DeokhugamException {

  public DuplicateBookException(String isbn) {
    super(
        ErrorCode.DUPLICATE_BOOK,
        Map.of("isbn", isbn)
    );
  }
}