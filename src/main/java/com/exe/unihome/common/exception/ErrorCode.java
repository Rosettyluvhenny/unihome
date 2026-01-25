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
  FURNITURE_OUT_OF_STOCK(2004, "Furniture is out of stock", HttpStatus.BAD_REQUEST),
  CART_NOT_FOUND(3000, "Cart not found", HttpStatus.NOT_FOUND),
  CART_ITEM_NOT_FOUND(3001, "Cart item not found", HttpStatus.NOT_FOUND),
  CART_ITEM_EXISTS(3002, "Furniture already in cart", HttpStatus.BAD_REQUEST),
  CART_EMPTY(3003, "Cart is empty", HttpStatus.BAD_REQUEST),
  ORDER_NOT_FOUND(4000, "Order not found", HttpStatus.NOT_FOUND),
  ORDER_STATUS_INVALID(4001, "Order status is invalid", HttpStatus.BAD_REQUEST),
  ORDER_STATUS_TRANSITION_INVALID(4002, "Cannot transition order to requested status", HttpStatus.BAD_REQUEST),
  ORDER_ALREADY_CANCELLED(4003, "Order already cancelled", HttpStatus.BAD_REQUEST),
  USER_LOCATION_NOT_SET(1022, "User location not set", HttpStatus.BAD_REQUEST),
  WAREHOUSE_LOCATION_NOT_CONFIGURED(1023, "Warehouse location not configured", HttpStatus.INTERNAL_SERVER_ERROR),
  SHIPPING_TIER_NOT_FOUND(5000, "Shipping fee configuration not found", HttpStatus.INTERNAL_SERVER_ERROR),
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
