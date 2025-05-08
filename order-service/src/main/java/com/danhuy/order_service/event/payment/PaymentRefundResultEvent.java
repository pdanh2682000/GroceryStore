package com.danhuy.order_service.event.payment;

import lombok.Data;

@Data
public class PaymentRefundResultEvent {

  private String orderId;
  private boolean success;
  private String message;
}
