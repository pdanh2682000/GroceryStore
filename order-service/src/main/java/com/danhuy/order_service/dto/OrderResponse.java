package com.danhuy.order_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderResponse {

  private String id;
  private String userId;
  private String status;
  private LocalDateTime orderDate;
  private BigDecimal totalAmount;
  private List<OrderItemResponse> items;

  @Data
  public static class OrderItemResponse {

    private String productId;
    private Integer quantity;
    private BigDecimal price;
  }
}
