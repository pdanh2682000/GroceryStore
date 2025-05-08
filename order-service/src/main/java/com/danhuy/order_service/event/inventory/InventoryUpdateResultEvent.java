package com.danhuy.order_service.event.inventory;

import lombok.Data;

@Data
public class InventoryUpdateResultEvent {

  private String orderId;
  private boolean success;
  private String message;
}