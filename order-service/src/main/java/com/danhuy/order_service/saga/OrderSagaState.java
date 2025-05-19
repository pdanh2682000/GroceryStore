package com.danhuy.order_service.saga;

import com.danhuy.common_service.dto.OrderItemDto;
import com.danhuy.common_service.enums.PaymentMethod;
import com.danhuy.common_service.enums.SagaStep;
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
  private PaymentMethod paymentMethod;
}
