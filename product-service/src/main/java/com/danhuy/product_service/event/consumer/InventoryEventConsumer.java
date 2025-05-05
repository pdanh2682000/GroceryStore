package com.danhuy.product_service.event.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

  // Dùng để xử lý các sự kiện từ inventory-service nếu cần
  // Ví dụ: cập nhật trạng thái hàng tồn kho cho sản phẩm
  @KafkaListener(topics = "${kafka.topics.inventory-updated}", groupId = "${spring.kafka.consumer.group-id}")
  public void consumeInventoryUpdatedEvent(String message) {
    log.info("Received inventory update event: {}", message);
    // Xử lý logic khi nhận được sự kiện cập nhật hàng tồn kho
  }
}
