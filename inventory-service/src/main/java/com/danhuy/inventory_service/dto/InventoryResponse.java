package com.danhuy.inventory_service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

  private Long id;
  private Long productId;
  private Integer quantity;
  private Integer reservedQuantity;
  private Integer availableQuantity;
  private LocalDateTime updatedAt;
}