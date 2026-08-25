package com.deokhugam.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  DUPLICATE_BOOK(
          HttpStatus.CONFLICT,
          "이미 등록된 ISBN입니다."
  ),

  ISBN_OCR_FAILED(
    HttpStatus.BAD_REQUEST,
    "이미지에서 ISBN을 인식할 수 없습니다."
  ),

  BOOK_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "도서를 찾을 수 없습니다."
  ),

  COMMENT_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "댓글을 찾을 수 없습니다."
  ),

  COMMENT_ACCESS_DENIED(
          HttpStatus.FORBIDDEN,
          "본인이 작성한 댓글만 수정하거나 삭제할 수 있습니다."
  ),

  COMMENT_ALREADY_DELETED(
          HttpStatus.BAD_REQUEST,
          "삭제된 댓글은 수정할 수 없습니다."
  ),

  // user 도메인 예외 추가
  EMAIL_DUPLICATION(
          HttpStatus.CONFLICT,
          "이미 존재하는 이메일입니다."
  ),

  LOGIN_INPUT_INVALID(HttpStatus.UNAUTHORIZED,
          "이메일 또는 비밀번호가 올바르지 않습니다."
  ),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND,
          "사용자를 찾을 수 없습니다."
  ),
  // review 도메인 예외 추가
  DUPLICATE_REVIEW(
          HttpStatus.CONFLICT,
          "이미 작성된 리뷰가 존재합니다."
  ),

  REVIEW_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "리뷰를 찾을 수 없습니다."
  ),

  REVIEW_ACCESS_DENIED(
          HttpStatus.FORBIDDEN,
          "리뷰에 대한 권한이 없습니다."
  ),

  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "서버 내부 오류가 발생했습니다."
  ),

  // 알림 도메인 예외 추가
  NOTIFICATION_NOT_FOUND(
      HttpStatus.NOT_FOUND,
          "알림을 찾을 수 없습니다."
  ),

  // 403 Forbidden 관련 에러
  FORBIDDEN(
      HttpStatus.FORBIDDEN,
      "접근 권한이 없습니다."
  );

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}