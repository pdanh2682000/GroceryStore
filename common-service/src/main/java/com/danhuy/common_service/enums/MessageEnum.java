package com.danhuy.common_service.enums;

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
  CREATE_PRODUCT_SUCCESS(2002, "Create product success!", HttpStatus.CREATED),
  UPDATE_PRODUCT_SUCCESS(2003, "Update product success!", HttpStatus.ACCEPTED),
  CREATE_ORDER_SUCCESS(3001, "Create order success!", HttpStatus.CREATED),
  ORDER_NOT_EXISTED(3002, "Order not existed with orderId: {0}", HttpStatus.NOT_FOUND),
  NOT_ENOUGH_RESERVE_QUANTITY(4000, "Not enough quantity available! {0}", HttpStatus.BAD_REQUEST),
  NOT_RELEASE_RESERVE_QUANTITY(4001, "Cannot release more than reserved!", HttpStatus.BAD_REQUEST),
  NOT_REDUCE_QUANTITY(4002, "Cannot reduce more than available!", HttpStatus.BAD_REQUEST),
  INVENTORY_EXISTED(4003, "Inventory for product ID: {0} already exists!", HttpStatus.BAD_REQUEST),
  INVENTORY_NOT_EXISTED(4004, "Inventory not found for product ID: {0}", HttpStatus.NOT_FOUND),
  IN_STOCK(4005, "All items in stock", HttpStatus.OK),
  OUT_OF_STOCK(4006, "Contain items out of stock", HttpStatus.BAD_REQUEST),
  RESERVE_INVENTORY_SUCCESS(4007, "Inventory reserved successfully!", HttpStatus.OK),
  CREATE_INVENTORY_SUCCESS(4008, "Create inventory for product success!", HttpStatus.CREATED),
  UPDATE_INVENTORY_SUCCESS(4009, "Update inventory for product success!", HttpStatus.ACCEPTED),
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
