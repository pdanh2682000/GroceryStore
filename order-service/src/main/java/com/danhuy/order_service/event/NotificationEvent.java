package com.danhuy.order_service.event;

import lombok.Data;

@Data
public class NotificationEvent {

  private String orderId;
  private String userId;
  private String message;
}
