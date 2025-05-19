package com.danhuy.common_service.event.inventory;

import com.danhuy.common_service.dto.OrderItemDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckEvent {

  private String orderId;
  private List<OrderItemDto> orderItems;
}
