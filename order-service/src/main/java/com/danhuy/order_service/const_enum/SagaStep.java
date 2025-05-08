package com.danhuy.order_service.const_enum;

public enum SagaStep {
  CREATE_ORDER,
  CHECK_INVENTORY,
  PROCESS_PAYMENT,
  UPDATE_INVENTORY,
  ORDER_COMPLETED,
  REFUND_PAYMENT,
  ORDER_CANCELLED
}
