package com.danhuy.order_service.event;

import com.danhuy.order_service.dto.OrderItemDto;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderCreatedEvent {

  private String orderId;
  private String userId;
  private List<OrderItemDto> orderItems;
  private BigDecimal orderAmount;
}
