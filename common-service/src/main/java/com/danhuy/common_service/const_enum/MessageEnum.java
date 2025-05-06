package com.danhuy.common_service.const_enum;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum MessageEnum {

  UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR),
  VALIDATION_EXCEPTION(8888, "Validation error!", HttpStatus.BAD_REQUEST),
  RESOURCE_NOT_FOUND_EXCEPTION(7777, "Resource not existed!", HttpStatus.NOT_FOUND),
  NOT_FOUND_EXCEPTION(6666, "Not found exception!", HttpStatus.NOT_FOUND),
  VIOLATION_EXCEPTION(5555, "Violation exception!", HttpStatus.BAD_REQUEST),
  PRODUCT_EXISTED(2000, "Product existed!", HttpStatus.BAD_REQUEST),
  PRODUCT_NOT_EXISTED(2001, "Product not existed!", HttpStatus.NOT_FOUND),
  CREATE_PRODUCT_SUCCESS(2001, "Create product success!", HttpStatus.CREATED),
  UPDATE_PRODUCT_SUCCESS(2001, "Update product success!", HttpStatus.ACCEPTED),
  INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
  USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
  USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
  INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
  USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
  UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
  UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
  INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
  ;

  private final int code;
  private final String message;
  private final HttpStatusCode httpStatusCode;

  MessageEnum(int code, String message, HttpStatusCode httpStatusCode) {
    this.code = code;
    this.message = message;
    this.httpStatusCode = httpStatusCode;
  }

}
