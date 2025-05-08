package com.danhuy.order_service.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {

  private String userId;
  private List<OrderItemDto> orderItems;

  public BigDecimal calculateTotalAmount() {
    return orderItems.stream()
        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}

