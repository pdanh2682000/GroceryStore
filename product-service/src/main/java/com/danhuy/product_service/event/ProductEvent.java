package com.danhuy.product_service.event;

import com.danhuy.common_service.enums.EventType;
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
public class ProductEvent {

  private EventType eventType;
  private Long productId;
  private String productName;
  private BigDecimal price;
  private String description;
  private Long categoryId;
  private String categoryName;
  private LocalDateTime timestamp;

}
