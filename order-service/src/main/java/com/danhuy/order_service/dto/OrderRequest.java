package com.danhuy.order_service.dto;

import com.danhuy.common_service.dto.OrderItemDto;
import com.danhuy.common_service.enums.PaymentMethod;
import com.danhuy.common_service.validate.EnumValue;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {

  private String userId;
  private List<OrderItemDto> orderItems;
  @EnumValue(name = "paymentMethod", enumClass = PaymentMethod.class)
  private String paymentMethod;

  public BigDecimal calculateTotalAmount() {
    return orderItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}

