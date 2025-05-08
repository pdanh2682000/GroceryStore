package com.danhuy.order_service.saga;

import com.danhuy.order_service.const_enum.SagaStep;
import com.danhuy.order_service.dto.OrderItemDto;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderSagaState {

  private String sagaId;
  private String orderId;
  private String userId;
  private List<OrderItemDto> orderItems;
  private BigDecimal orderAmount;
  private SagaStep currentStep;
}
