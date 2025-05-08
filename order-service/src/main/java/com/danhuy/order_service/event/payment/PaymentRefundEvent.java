package com.danhuy.order_service.event.payment;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentRefundEvent {

  private String orderId;
  private String userId;
  private BigDecimal amount;
}
