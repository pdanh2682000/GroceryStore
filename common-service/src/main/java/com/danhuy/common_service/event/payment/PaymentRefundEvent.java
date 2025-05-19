package com.danhuy.common_service.event.payment;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundEvent {

  private String orderId;
  private String userId;
  private BigDecimal amount;
}
