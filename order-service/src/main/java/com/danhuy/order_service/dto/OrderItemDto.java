package com.danhuy.order_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemDto {

  private String productId;
  private Integer quantity;
  private BigDecimal price;
}