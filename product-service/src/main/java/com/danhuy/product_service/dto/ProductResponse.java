package com.danhuy.product_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private Long id;
  private String name;
  private String description;
  private BigDecimal price;
  private CategoryDto category;
  private String imageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean isActive;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryDto {

    private Long id;
    private String name;
  }
}
