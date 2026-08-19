package com.deokhugam.global.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String code,
    String exceptionType,
    String message,
    Map<String, Object> details
) {

  public static ErrorResponse of(
      int status,
      String code,
      String exceptionType,
      String message,
      Map<String, Object> details
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        code,
        exceptionType,
        message,
        details != null ? details : Map.of()
    );
  }

  public static ErrorResponse of(
      int status,
      String message
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        "COMMON_ERROR",
        "Exception",
        message,
        Map.of()
    );
  }
}