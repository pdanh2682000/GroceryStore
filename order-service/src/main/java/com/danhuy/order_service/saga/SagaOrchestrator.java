package com.danhuy.order_service.saga;

import com.danhuy.common_service.uilts.Pair;
import com.danhuy.order_service.const_enum.SagaStep;
import com.danhuy.order_service.event.NotificationEvent;
import com.danhuy.order_service.event.OrderCreatedEvent;
import com.danhuy.order_service.event.inventory.InventoryCheckEvent;
import com.danhuy.order_service.event.inventory.InventoryCheckResultEvent;
import com.danhuy.order_service.event.inventory.InventoryUpdateEvent;
import com.danhuy.order_service.event.inventory.InventoryUpdateResultEvent;
import com.danhuy.order_service.event.payment.PaymentRefundEvent;
import com.danhuy.order_service.event.payment.PaymentRefundResultEvent;
import com.danhuy.order_service.event.payment.PaymentRequestEvent;
import com.danhuy.order_service.event.payment.PaymentResultEvent;
import com.danhuy.order_service.logic.UpdateOrderStatusLogic;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestrator {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final UpdateOrderStatusLogic orderService;

  // Lưu trạng thái transaction theo orderId
  private final Map<String, OrderSagaState> sagaStateMap = new HashMap<>();

  @Value("${kafka.topics.inventory-check}")
  private String INVENTORY_CHECK;

  @Value("${kafka.topics.inventory-check-result}")
  private String INVENTORY_CHECK_RESULT;

  @Value("${kafka.topics.payment-request}")
  private String PAYMENT_REQUEST;

  @Value("${kafka.topics.payment-request-result}")
  private String PAYMENT_REQUEST_RESULT;

  @Value("${kafka.topics.inventory-update}")
  private String INVENTORY_UPDATE;

  @Value("${kafka.topics.inventory-update-result}")
  private String INVENTORY_UPDATE_RESULT;

  @Value("${kafka.topics.payment-refund}")
  private String PAYMENT_REFUND;

  @Value("${kafka.topics.payment-refund-result}")
  private String PAYMENT_REFUND_RESULT;

  @Value("${kafka.topics.notification}")
  private String NOTIFICATION;

  /**
   * start order saga when order created.
   *
   * @param orderCreatedEvent OrderCreatedEvent
   */
  public void startCreateOrderSaga(OrderCreatedEvent orderCreatedEvent) {
    String sagaId = UUID.randomUUID().toString();
    log.info("Starting create order saga with ID: {}", sagaId);

    // Khởi tạo trạng thái saga mới
    String orderId = orderCreatedEvent.getOrderId();
    OrderSagaState sagaState = new OrderSagaState();
    sagaState.setSagaId(sagaId);
    sagaState.setOrderId(orderId);
    sagaState.setCurrentStep(SagaStep.CREATE_ORDER);
    sagaStateMap.put(orderId, sagaState);

    // Bước tiếp theo: Kiểm tra inventory
    validateInventory(orderCreatedEvent);
  }

  /**
   * produce a message for inventory-service to check quantity.
   *
   * @param orderCreatedEvent OrderCreatedEvent
   */
  private void validateInventory(OrderCreatedEvent orderCreatedEvent) {
    log.info("Sending inventory check request for order: {}", orderCreatedEvent.getOrderId());
    String orderId = orderCreatedEvent.getOrderId();

    InventoryCheckEvent inventoryEvent = new InventoryCheckEvent();
    inventoryEvent.setOrderId(orderId);
    inventoryEvent.setOrderItems(orderCreatedEvent.getOrderItems());

    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);
    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    OrderSagaState state = stateMap.getFirst();
    state.setCurrentStep(SagaStep.CHECK_INVENTORY);

    kafkaTemplate.send(INVENTORY_CHECK, inventoryEvent);
  }

  /**
   * consume a message from response inventory-service when checked
   *
   * @param resultEvent InventoryCheckResultEvent
   */
  @KafkaListener(topics = "${kafka.topics.inventory-check-result}")
  public void handleInventoryCheckResult(InventoryCheckResultEvent resultEvent) {
    String orderId = resultEvent.getOrderId();
    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);

    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    OrderSagaState state = stateMap.getFirst();

    if (resultEvent.isAvailable()) {
      log.info("Inventory available for order: {}", orderId);
      // Bước tiếp theo: Xử lý thanh toán
      processPayment(orderId, state);
    } else {
      log.error("Inventory not available for order: {}", orderId);
      // Không đủ tồn kho, rollback và kết thúc saga
      cancelOrder(orderId, "Insufficient inventory");
    }
  }

  /**
   * produce a message for payment-service to process payment.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void processPayment(String orderId, OrderSagaState state) {
    log.info("Processing payment for order: {}", orderId);

    PaymentRequestEvent paymentEvent = new PaymentRequestEvent();
    paymentEvent.setOrderId(orderId);
    paymentEvent.setAmount(state.getOrderAmount());
    paymentEvent.setUserId(state.getUserId());

    state.setCurrentStep(SagaStep.PROCESS_PAYMENT);

    kafkaTemplate.send(PAYMENT_REQUEST, paymentEvent);
  }

  /**
   * consume a message from response payment-service when paid
   *
   * @param resultEvent PaymentResultEvent
   */
  @KafkaListener(topics = "${kafka.topics.payment-request-result}")
  public void handlePaymentResult(PaymentResultEvent resultEvent) {
    String orderId = resultEvent.getOrderId();
    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);

    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    OrderSagaState state = stateMap.getFirst();

    if (resultEvent.isSuccess()) {
      log.info("Payment successful for order: {}", orderId);
      // Bước tiếp theo: Cập nhật hàng tồn kho
      updateInventory(orderId, state);
    } else {
      log.error("Payment failed for order: {}", orderId);
      // Thanh toán thất bại, rollback và kết thúc saga
      cancelOrder(orderId, "Payment failed");
    }
  }

  /**
   * produce a message for inventory-service to update inventory.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void updateInventory(String orderId, OrderSagaState state) {
    log.info("Updating inventory for order: {}", orderId);

    InventoryUpdateEvent updateEvent = new InventoryUpdateEvent();
    updateEvent.setOrderId(orderId);
    updateEvent.setOrderItems(state.getOrderItems());

    state.setCurrentStep(SagaStep.UPDATE_INVENTORY);

    kafkaTemplate.send(INVENTORY_UPDATE, updateEvent);
  }

  /**
   * consume a message from response inventory-service when updated.
   *
   * @param resultEvent InventoryUpdateResultEvent
   */
  @KafkaListener(topics = "${kafka.topics.inventory-update-result}")
  public void handleInventoryUpdateResult(InventoryUpdateResultEvent resultEvent) {
    String orderId = resultEvent.getOrderId();
    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);

    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    OrderSagaState state = stateMap.getFirst();

    if (resultEvent.isSuccess()) {
      log.info("Inventory updated successfully for order: {}", orderId);
      // Hoàn thành saga
      completeOrder(orderId, state);
    } else {
      log.error("Inventory update failed for order: {}", orderId);
      // Hoàn trả payment và rollback
      refundPayment(orderId, state);
    }
  }

  /**
   * produce a message for notification-service to notify and terminate SAGA.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void completeOrder(String orderId, OrderSagaState state) {
    log.info("Completing order: {}", orderId);

    state.setCurrentStep(SagaStep.ORDER_COMPLETED);

    // Cập nhật trạng thái đơn hàng thành COMPLETED
    orderService.updateOrderStatus(orderId, "COMPLETED", null);

    // Gửi thông báo cho người dùng
    sendNotification(orderId, "Your order has been placed successfully!");

    // Xóa saga state khi hoàn thành
    sagaStateMap.remove(orderId);
  }

  /**
   * produce a message for payment-service to refund.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void refundPayment(String orderId, OrderSagaState state) {
    log.info("Initiating payment refund for order: {}", orderId);

    PaymentRefundEvent refundEvent = new PaymentRefundEvent();
    refundEvent.setOrderId(orderId);
    refundEvent.setAmount(state.getOrderAmount());
    refundEvent.setUserId(state.getUserId());

    state.setCurrentStep(SagaStep.REFUND_PAYMENT);

    kafkaTemplate.send(PAYMENT_REFUND, refundEvent);
  }

  /**
   * consume a message from response payment-service when refunded.
   *
   * @param resultEvent PaymentRefundResultEvent
   */
  @KafkaListener(topics = "${kafka.topics.payment-refund-result}")
  public void handlePaymentRefundResult(PaymentRefundResultEvent resultEvent) {
    String orderId = resultEvent.getOrderId();
    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);

    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    // Dù refund có thành công hay không, ta vẫn phải cancel order
    cancelOrder(orderId, "Failed to update inventory, payment has been refunded");
  }

  /**
   * cancel order and product a message to notification-service to notify.
   *
   * @param orderId String
   * @param reason  String
   */
  private void cancelOrder(String orderId, String reason) {
    log.info("Cancelling order: {} due to: {}", orderId, reason);
    Pair<OrderSagaState, Boolean> stateMap = getSagaState(orderId);

    if (Boolean.FALSE.equals(stateMap.getSecond())) {
      return;
    }

    OrderSagaState state = stateMap.getFirst();
    state.setCurrentStep(SagaStep.ORDER_CANCELLED);

    // Cập nhật trạng thái đơn hàng thành CANCELLED
    orderService.updateOrderStatus(orderId, "CANCELLED", reason);

    // Gửi thông báo cho người dùng
    sendNotification(orderId, "Your order has been cancelled: " + reason);

    // Xóa saga state khi hoàn thành
    sagaStateMap.remove(orderId);
  }

  /**
   * produce a message for payment-service to refund.
   *
   * @param orderId String
   * @param message String
   */
  private void sendNotification(String orderId, String message) {
    NotificationEvent notificationEvent = new NotificationEvent();
    notificationEvent.setOrderId(orderId);
    notificationEvent.setMessage(message);

    kafkaTemplate.send(NOTIFICATION, notificationEvent);
  }

  private Pair<OrderSagaState, Boolean> getSagaState(String orderId) {
    OrderSagaState state = sagaStateMap.get(orderId);

    if (state == null) {
      log.error("No saga state found for order ID: {}", orderId);
      return Pair.of(null, false);
    }
    return Pair.of(state, true);
  }
}
