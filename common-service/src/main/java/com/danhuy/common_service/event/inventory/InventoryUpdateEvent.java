package com.danhuy.common_service.event.inventory;

import com.danhuy.common_service.dto.OrderItemDto;
import com.danhuy.common_service.enums.InventoryUpdateType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateEvent {

  private String orderId;
  private List<OrderItemDto> orderItems;
  private InventoryUpdateType updateType;

}
