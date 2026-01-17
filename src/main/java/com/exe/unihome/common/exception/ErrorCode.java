package com.exe.unihome.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
  INVALID_REQUEST(1000, "Invalid request", HttpStatus.BAD_REQUEST),
  UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  USER_NOT_FOUND(1002, "User not found", HttpStatus.NOT_FOUND),
  INVALID_CREDENTIALS(1003, "Invalid credentials", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_INVALID(1004, "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
  REFRESH_TOKEN_EXPIRED(1005, "Refresh token is expired", HttpStatus.UNAUTHORIZED),
  EMAIL_ALREADY_EXISTS(1006, "Email already exists", HttpStatus.BAD_REQUEST),
  VERIFY_TOKEN_INVALID(1007, "Verify token is invalid", HttpStatus.UNAUTHORIZED),
  VERIFY_TOKEN_EXPIRED(1008, "Verify token is expired", HttpStatus.UNAUTHORIZED),
  FAIL_MAIL_ENQUEUED(1009, "Failed to enqueue mail job", HttpStatus.BAD_REQUEST),
  FAIL_MAIL_RETRY(1010, "Failed to retry mail job", HttpStatus.BAD_REQUEST),
  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_TOKEN(1011, "Invalid token", HttpStatus.UNAUTHORIZED),
  TOKEN_PARSE_ERROR(1012, "PARSE TOKEN FAILED", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED(1013, "Unauthorized", HttpStatus.UNAUTHORIZED),
  ALREADY_SENT(1014, "Mail already sent, check you spam", HttpStatus.BAD_REQUEST),
  USER_VERIFIED(1015, "User is already activated", HttpStatus.BAD_REQUEST),
  USER_NOT_VERIFIED(1016, "Please check your mail for account verification", HttpStatus.BAD_REQUEST),
  PHONE_ALREADY_EXISTS(1017, "Phone already exists", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD(1018, "Current password is incorrect", HttpStatus.UNAUTHORIZED),
  PASSWORD_MISMATCH(1019, "New password and confirm password do not match", HttpStatus.BAD_REQUEST),
  FURNITURE_NOT_FOUND(2001, "Furniture not found", HttpStatus.NOT_FOUND),
  DISCOUNT_ALREADY_APPLIED(2002, "Discount already applied to this furniture", HttpStatus.BAD_REQUEST),
  DISCOUNT_NOT_FOUND(2003, "Discount not found", HttpStatus.NOT_FOUND),
  NOTI_NOT_FOUND(1020, "Notification not existed", HttpStatus.NOT_FOUND),
  ROOM_NOT_FOUND(1021, "Room not existed", HttpStatus.NOT_FOUND),
  ;

  private final int code;
  private final String message;
  private final HttpStatusCode statusCode;

  ErrorCode(int code, String message, HttpStatusCode statusCode) {
    this.code = code;
    this.message = message;
    this.statusCode = statusCode;
  }
}
