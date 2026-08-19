package com.deokhugam.review.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class DuplicateReviewException extends DeokhugamException {

  public DuplicateReviewException(
          UUID userId,
          UUID bookId
  ) {
    super(
            ErrorCode.DUPLICATE_REVIEW,
            Map.of(
                    "userId", userId,
                    "bookId", bookId
            )
    );
  }
}