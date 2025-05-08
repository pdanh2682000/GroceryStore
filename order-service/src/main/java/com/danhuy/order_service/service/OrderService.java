package com.danhuy.order_service.service;

import com.danhuy.common_service.const_enum.MessageEnum;
import com.danhuy.common_service.exception.ex.AppException;
import com.danhuy.order_service.dto.OrderRequest;
import com.danhuy.order_service.dto.OrderResponse;
import com.danhuy.order_service.entity.Order;
import com.danhuy.order_service.entity.OrderItem;
import com.danhuy.order_service.event.OrderCreatedEvent;
import com.danhuy.order_service.logic.UpdateOrderStatusLogic;
import com.danhuy.order_service.repository.OrderRepository;
import com.danhuy.order_service.saga.SagaOrchestrator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderRepository orderRepository;
  private final SagaOrchestrator sagaOrchestrator;
  private final UpdateOrderStatusLogic updateOrderStatusLogic;

  /**
   * Create a new order and communicate with other services in SAGA transaction.
   *
   * @param orderRequest OrderRequest
   * @return OrderResponse
   */
  @Transactional
  public OrderResponse createOrder(OrderRequest orderRequest) {
    log.info("Creating new order for user: {}", orderRequest.getUserId());

    // Tạo đối tượng Order mới
    Order order = new Order();
    order.setId(UUID.randomUUID().toString());
    order.setUserId(orderRequest.getUserId());
    order.setStatus("PENDING");
    order.setOrderDate(LocalDateTime.now());
    order.setTotalAmount(orderRequest.calculateTotalAmount());

    // Tạo danh sách OrderItem
    List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
        .map(itemDto -> {
          OrderItem item = new OrderItem();
          item.setProductId(itemDto.getProductId());
          item.setQuantity(itemDto.getQuantity());
          item.setPrice(itemDto.getPrice());
          item.setOrder(order);
          return item;
        })
        .collect(Collectors.toList());

    order.setItems(orderItems);

    // Lưu order vào database
    Order savedOrder = orderRepository.save(order);

    // Tạo và gửi event để bắt đầu saga
    OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
    orderCreatedEvent.setOrderId(savedOrder.getId());
    orderCreatedEvent.setUserId(savedOrder.getUserId());
    orderCreatedEvent.setOrderItems(orderRequest.getOrderItems());
    orderCreatedEvent.setOrderAmount(savedOrder.getTotalAmount());

    // Khởi động saga process
    sagaOrchestrator.startCreateOrderSaga(orderCreatedEvent);

    // Trả về thông tin order đã tạo
    return mapToOrderResponse(savedOrder);
  }

  /**
   * Update status order when complete payment or cancel order in SAGA transaction.
   *
   * @param orderId String
   * @param status  String
   */
  @Transactional
  public void updateOrderStatus(String orderId, String status) {
    updateOrderStatusLogic.updateOrderStatus(orderId, status, null);
  }

  /**
   * Get information order from orderID
   *
   * @param orderId String
   * @return OrderResponse
   */
  public OrderResponse getOrder(String orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new AppException(MessageEnum.ORDER_NOT_EXISTED, orderId));

    return mapToOrderResponse(order);
  }

  /**
   * Get information order from userId.
   *
   * @param userId String
   * @return List<OrderResponse>
   */
  public List<OrderResponse> getOrdersByUserId(String userId) {
    List<Order> orders = orderRepository.findByUserId(userId);

    return orders.stream()
        .map(this::mapToOrderResponse)
        .collect(Collectors.toList());
  }

  private OrderResponse mapToOrderResponse(Order order) {
    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setUserId(order.getUserId());
    response.setStatus(order.getStatus());
    response.setOrderDate(order.getOrderDate());
    response.setTotalAmount(order.getTotalAmount());

    // Map order items
    response.setItems(order.getItems().stream()
        .map(item -> {
          OrderResponse.OrderItemResponse itemResponse = new OrderResponse.OrderItemResponse();
          itemResponse.setProductId(item.getProductId());
          itemResponse.setQuantity(item.getQuantity());
          itemResponse.setPrice(item.getPrice());
          return itemResponse;
        })
        .collect(Collectors.toList()));

    return response;
  }
}