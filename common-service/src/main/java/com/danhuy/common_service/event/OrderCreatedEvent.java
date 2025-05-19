package com.danhuy.common_service.event;

import com.danhuy.common_service.dto.OrderItemDto;
import com.danhuy.common_service.enums.PaymentMethod;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

  private String orderId;
  private String userId;
  private List<OrderItemDto> orderItems;
  private BigDecimal orderAmount;
  private PaymentMethod paymentMethod;
}
