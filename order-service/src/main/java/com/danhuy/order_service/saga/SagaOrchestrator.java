package com.danhuy.order_service.saga;

import com.danhuy.common_service.enums.InventoryUpdateType;
import com.danhuy.common_service.enums.PaymentMethod;
import com.danhuy.common_service.enums.SagaStep;
import com.danhuy.common_service.event.NotificationEvent;
import com.danhuy.common_service.event.OrderCreatedEvent;
import com.danhuy.common_service.event.inventory.InventoryCheckEvent;
import com.danhuy.common_service.event.inventory.InventoryCheckResultEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateResultEvent;
import com.danhuy.common_service.event.payment.PaymentRefundEvent;
import com.danhuy.common_service.event.payment.PaymentRefundResultEvent;
import com.danhuy.common_service.event.payment.PaymentRequestEvent;
import com.danhuy.common_service.event.payment.PaymentResultEvent;
import com.danhuy.common_service.uilts.Pair;
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
    sagaState.setOrderItems(orderCreatedEvent.getOrderItems());
    sagaState.setOrderAmount(orderCreatedEvent.getOrderAmount());
    sagaState.setUserId(orderCreatedEvent.getUserId());
    sagaState.setPaymentMethod(orderCreatedEvent.getPaymentMethod());
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
      // Bước tiếp theo: Đặt trước hàng tồn kho (RESERVE)
      reserveInventory(orderId, state);
    } else {
      log.error("Inventory not available for order: {}", orderId);
      // Không đủ tồn kho, rollback và kết thúc saga
      cancelOrder(orderId, "Insufficient inventory");
    }
  }

  /**
   * produce a message for inventory-service to reserve inventory.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void reserveInventory(String orderId, OrderSagaState state) {
    log.info("Reserving inventory for order: {}", orderId);

    InventoryUpdateEvent updateEvent = new InventoryUpdateEvent();
    updateEvent.setOrderId(orderId);
    updateEvent.setOrderItems(state.getOrderItems());
    // Set update type to RESERVE
    updateEvent.setUpdateType(InventoryUpdateType.RESERVE);

    state.setCurrentStep(SagaStep.RESERVE_INVENTORY);

    kafkaTemplate.send(INVENTORY_UPDATE, updateEvent);
  }

  /**
   * consume a message from response inventory-service when reserved.
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

    // Xử lý khác nhau tùy vào loại update và trạng thái hiện tại
    switch (resultEvent.getUpdateType()) {
      case RESERVE:
        handleReserveInventoryResult(resultEvent, orderId, state);
        break;
      case COMMIT:
        handleCommitInventoryResult(resultEvent, orderId, state);
        break;
      case RELEASE:
        handleReleaseInventoryResult(resultEvent, orderId, state);
        break;
      default:
        log.error("Unknown update type: {}", resultEvent.getUpdateType());
    }
  }

  /**
   * Handle the result of RESERVE inventory operation
   */
  private void handleReserveInventoryResult(InventoryUpdateResultEvent resultEvent, String orderId,
      OrderSagaState state) {
    if (resultEvent.isSuccess()) {
      log.info("Inventory reserved successfully for order: {}", orderId);
      // Bước tiếp theo: Xử lý thanh toán
      processPayment(orderId, state);
    } else {
      log.error("Failed to reserve inventory for order: {}", orderId);
      // Không thể đặt chỗ, hủy đơn hàng
      cancelOrder(orderId, "Failed to reserve inventory");
    }
  }

  /**
   * Handle the result of COMMIT inventory operation
   */
  private void handleCommitInventoryResult(InventoryUpdateResultEvent resultEvent, String orderId,
      OrderSagaState state) {
    if (resultEvent.isSuccess()) {
      log.info("Inventory committed successfully for order: {}", orderId);
      // Hoàn thành saga
      completeOrder(orderId, state);
    } else {
      log.error("Failed to commit inventory for order: {}", orderId);
      // Hoàn trả payment và rollback
      refundPayment(orderId, state);
    }
  }

  /**
   * Handle the result of RELEASE inventory operation
   */
  private void handleReleaseInventoryResult(InventoryUpdateResultEvent resultEvent, String orderId,
      OrderSagaState state) {
    log.info("Inventory release processed for order: {} is {}", orderId, resultEvent.isSuccess());
    // Regardless of the result, the order is already in a cancellation state
    // We may want to add error handling if inventory release persistently fails
  }

  /**
   * produce a message for payment-service to process payment.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void processPayment(String orderId, OrderSagaState state) {
    if (state.getPaymentMethod() == PaymentMethod.CASH) {
      log.info("Pass payment for order: {} because payment method is {}", orderId,
          state.getPaymentMethod().name());
      // By pass step payment
      commitInventory(orderId, state);
      return;
    }

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
      // Bước tiếp theo: Cập nhật hàng tồn kho (COMMIT)
      commitInventory(orderId, state);
    } else {
      log.error("Payment failed for order: {}", orderId);
      // Thanh toán thất bại, giải phóng đặt chỗ (RELEASE) và kết thúc saga
      releaseInventory(orderId, state, "Payment failed");
    }
  }

  /**
   * produce a message for inventory-service to commit inventory.
   *
   * @param orderId String
   * @param state   OrderSagaState
   */
  private void commitInventory(String orderId, OrderSagaState state) {
    log.info("Committing inventory for order: {}", orderId);

    InventoryUpdateEvent updateEvent = new InventoryUpdateEvent();
    updateEvent.setOrderId(orderId);
    updateEvent.setOrderItems(state.getOrderItems());
    // Set update type to COMMIT
    updateEvent.setUpdateType(InventoryUpdateType.COMMIT);

    state.setCurrentStep(SagaStep.COMMIT_INVENTORY);

    kafkaTemplate.send(INVENTORY_UPDATE, updateEvent);
  }

  /**
   * produce a message for inventory-service to release reserved inventory.
   *
   * @param orderId String
   * @param state   OrderSagaState
   * @param reason  String
   */
  private void releaseInventory(String orderId, OrderSagaState state, String reason) {
    log.info("Releasing inventory for order: {} due to: {}", orderId, reason);

    InventoryUpdateEvent updateEvent = new InventoryUpdateEvent();
    updateEvent.setOrderId(orderId);
    updateEvent.setOrderItems(state.getOrderItems());
    // Set update type to RELEASE
    updateEvent.setUpdateType(InventoryUpdateType.RELEASE);

    state.setCurrentStep(SagaStep.RELEASE_INVENTORY);

    kafkaTemplate.send(INVENTORY_UPDATE, updateEvent);

    // Since we're in a failure path, we'll move directly to cancel the order
    cancelOrder(orderId, reason);
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

    OrderSagaState state = stateMap.getFirst();

    // Sau khi hoàn tiền, giải phóng đặt chỗ inventory
    releaseInventory(orderId, state, "Failed to commit inventory, payment has been refunded");
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
   * produce a message for notification-service to notify.
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