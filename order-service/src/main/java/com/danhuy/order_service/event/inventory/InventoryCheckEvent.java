package com.danhuy.order_service.event.inventory;

import com.danhuy.order_service.dto.OrderItemDto;
import java.util.List;
import lombok.Data;

@Data
public class InventoryCheckEvent {

  private String orderId;
  private List<OrderItemDto> orderItems;
}
