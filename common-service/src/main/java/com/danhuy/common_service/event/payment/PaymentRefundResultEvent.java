package com.danhuy.common_service.event.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundResultEvent {

  private String orderId;
  private boolean success;
  private String message;
}
