package com.deokhugam.global.exception;

import com.deokhugam.global.response.ErrorResponse;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
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
            errorCode.name(), // Enum 이름을 code로 사용
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

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException e
  ) {
    String errorMessage = e.getAllErrors().stream()
        .map(error -> error.getDefaultMessage())
        .filter(message -> message != null && !message.isBlank())
        .findFirst()
        .orElse("요청 값이 올바르지 않습니다.");

    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_INPUT_VALUE",
            "HandlerMethodValidationException",
            errorMessage,
            Map.of()
        ));
  }

  @ExceptionHandler({
          MissingRequestHeaderException.class,
          MissingServletRequestParameterException.class,
          MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponse> handleRequestBindingException(
          Exception e
  ) {
    return ResponseEntity
            .badRequest()
            .body(ErrorResponse.of(
                    HttpStatus.BAD_REQUEST.value(),
                    "INVALID_INPUT_VALUE",
                    e.getClass().getSimpleName(),
                    "요청 값이 올바르지 않습니다.",
                    Map.of()
            ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("예상하지 못한 예외가 발생했습니다.", e);

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "서버 내부 오류가 발생했습니다."
        ));
  }
}