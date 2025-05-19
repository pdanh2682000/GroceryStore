package com.danhuy.common_service.event.inventory;

import com.danhuy.common_service.enums.InventoryUpdateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateResultEvent {

  private String orderId;
  private boolean success;
  private String message;
  private InventoryUpdateType updateType;

}