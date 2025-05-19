package com.danhuy.product_service.event.producer;

import com.danhuy.common_service.enums.EventType;
import com.danhuy.product_service.entity.Product;
import com.danhuy.product_service.event.ProductEvent;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

  private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

  @Value("${kafka.topics.product-created}")
  private String productCreatedTopic;
  @Value("${kafka.topics.product-updated}")
  private String productUpdatedTopic;
  @Value("${kafka.topics.product-deleted}")
  private String productDeletedTopic;

  public void publishProductCreatedEvent(Product product) {
    ProductEvent event = setupProductEvent(product, EventType.CREATED);

    CompletableFuture<SendResult<String, ProductEvent>> future = kafkaTemplate.send(
        productCreatedTopic, product.getId().toString(), event);

    future.thenAccept(result -> log.info("Product created event sent successfully for product: {}",
            product.getId()))
        .exceptionally(
            ex -> {
              log.error("Failed to send product created event for product: {}", product.getId(),
                  ex);
              return null;
            });
  }

  public void publishProductUpdatedEvent(Product product) {
    ProductEvent event = setupProductEvent(product, EventType.UPDATED);

    CompletableFuture<SendResult<String, ProductEvent>> future = kafkaTemplate.send(
        productUpdatedTopic, product.getId().toString(), event);

    future.thenAccept(result -> log.info("Product updated event sent successfully for product: {}",
            product.getId()))
        .exceptionally(ex -> {
          log.error("Failed to send product updated event for product: {}", product.getId(), ex);
          return null;
        });
  }

  public void publishProductDeletedEvent(Product product) {
    ProductEvent event = setupProductEvent(product, EventType.DELETED);

    CompletableFuture<SendResult<String, ProductEvent>> future = kafkaTemplate.send(
        productDeletedTopic, product.getId().toString(), event);

    future.thenAccept(result -> log.info("Product deleted event sent successfully for product: {}",
            product.getId()))
        .exceptionally(ex -> {
          log.error("Failed to send product deleted event for product: {}", product.getId(), ex);
          return null;
        });
  }

  private ProductEvent setupProductEvent(Product product, EventType eventType) {
    return ProductEvent.builder()
        .eventType(eventType)
        .productId(product.getId())
        .productName(product.getName())
        .price(product.getPrice())
        .description(product.getDescription())
        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
        .timestamp(LocalDateTime.now())
        .build();
  }

}
