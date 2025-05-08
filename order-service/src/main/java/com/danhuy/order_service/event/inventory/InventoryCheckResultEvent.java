package com.danhuy.order_service.event.inventory;

import lombok.Data;

@Data
public class InventoryCheckResultEvent {

  private String orderId;
  private boolean available;
  private String message;
}
