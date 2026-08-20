package com.deokhugam.review.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewNotFoundException extends DeokhugamException {

  public ReviewNotFoundException(UUID reviewId) {
    super(
            ErrorCode.REVIEW_NOT_FOUND,
            Map.of("reviewId", reviewId)
    );
  }
}