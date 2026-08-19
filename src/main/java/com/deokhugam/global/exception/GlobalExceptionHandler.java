package com.deokhugam.global.exception;

import com.deokhugam.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DeokhugamException.class)
  public ResponseEntity<ErrorResponse> handleDeokhugamException(
      DeokhugamException e
  ) {
    ErrorCode errorCode = e.getErrorCode();

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ErrorResponse.of(
            errorCode.getStatus().value(),
            errorCode.name(), // Enun 이름을 code로 사용
            e.getClass().getSimpleName(), // 예외 클래스명
            errorCode.getMessage(),
            e.getDetails()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e
  ) {
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_INPUT_VALUE",
            "MethodArgumentNotValidException",
             errorMessage,
             Map.of()
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "서버 내부 오류가 발생했습니다."
        ));
  }
}