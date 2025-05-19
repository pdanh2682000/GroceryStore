package com.danhuy.inventory_service.kafka;

import com.danhuy.common_service.event.inventory.InventoryCheckEvent;
import com.danhuy.common_service.event.inventory.InventoryCheckResultEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateResultEvent;
import com.danhuy.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaListeners {

  private final InventoryService inventoryService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${kafka.topics.inventory-check-result}")
  private String INVENTORY_CHECK_RESULT;

  @Value("${kafka.topics.inventory-update-result}")
  private String INVENTORY_UPDATE_RESULT;

  /**
   * Check inventory when receive message inventory-check.
   *
   * @param request InventoryCheckRequest
   */
  @KafkaListener(
      topics = "${kafka.topics.inventory-check}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void handleInventoryCheck(InventoryCheckEvent request) {
    log.info("Received inventory check request for order: {}", request.getOrderId());

    try {
      // checking
      InventoryCheckResultEvent result = inventoryService.processInventoryCheck(request);
      // produce a message when checked
      kafkaTemplate.send(INVENTORY_CHECK_RESULT, request.getOrderId(), result);
      log.info("Sent inventory check result for order: {}, success: {}",
          request.getOrderId(), result.isAvailable());
    } catch (Exception e) {
      log.error("Error processing inventory check for order: {}", request.getOrderId(), e);
      InventoryCheckResultEvent errorResult = InventoryCheckResultEvent.builder()
          .orderId(request.getOrderId())
          .available(false)
          .message("Error processing inventory check: " + e.getMessage())
          .build();
      kafkaTemplate.send(INVENTORY_CHECK_RESULT, request.getOrderId(), errorResult);
    }
  }

  /**
   * Update inventory when receive message inventory-update based on InventoryUpdateType.
   *
   * @param request InventoryUpdateEvent
   */
  @KafkaListener(
      topics = "${kafka.topics.inventory-update}",
      groupId = "${spring.kafka.consumer.group-id}")
  public void handleInventoryUpdate(InventoryUpdateEvent request) {
    log.info("Received inventory update request for order: {}, type: {}",
        request.getOrderId(), request.getUpdateType());

    try {
      // updating
      InventoryUpdateResultEvent result = inventoryService.processInventoryUpdate(request);
      // produce a message when updated
      kafkaTemplate.send(INVENTORY_UPDATE_RESULT, request.getOrderId(), result);
      log.info("Sent inventory update result for order: {}, success: {}",
          request.getOrderId(), result.isSuccess());
    } catch (Exception e) {
      log.error("Error processing inventory update for order: {}", request.getOrderId(), e);
      InventoryUpdateResultEvent errorResult = InventoryUpdateResultEvent.builder()
          .orderId(request.getOrderId())
          .success(false)
          .message("Error processing inventory update: " + e.getMessage())
          .updateType(request.getUpdateType())
          .build();
      kafkaTemplate.send(INVENTORY_UPDATE_RESULT, request.getOrderId(), errorResult);
    }
  }
}