package com.danhuy.order_service.logic;

import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.exception.ex.AppException;
import com.danhuy.order_service.entity.Order;
import com.danhuy.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateOrderStatusLogic {

  private final OrderRepository orderRepository;

  /**
   * Logic update status order.
   *
   * @param orderId String
   * @param status  String
   * @param reason  String
   */
  @Transactional
  public void updateOrderStatus(String orderId, String status, String reason) {
    log.info("Updating order status for ID: {} to: {}", orderId, status);

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new AppException(MessageEnum.ORDER_NOT_EXISTED, orderId));

    order.setStatus(status);
    if (reason != null) {
      order.setNotes(reason);
    }

    orderRepository.save(order);
  }

}
