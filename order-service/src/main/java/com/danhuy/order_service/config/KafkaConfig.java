package com.danhuy.order_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

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

  @Bean
  public NewTopic inventoryCheckTopic() {
    return TopicBuilder.name(INVENTORY_CHECK)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic inventoryCheckResultTopic() {
    return TopicBuilder.name(INVENTORY_CHECK_RESULT)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentRequestTopic() {
    return TopicBuilder.name(PAYMENT_REQUEST)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentRequestResultTopic() {
    return TopicBuilder.name(PAYMENT_REQUEST_RESULT)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic inventoryUpdateTopic() {
    return TopicBuilder.name(INVENTORY_UPDATE)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic inventoryUpdateResultTopic() {
    return TopicBuilder.name(INVENTORY_UPDATE_RESULT)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentRefundTopic() {
    return TopicBuilder.name(PAYMENT_REFUND)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic paymentRefundResultTopic() {
    return TopicBuilder.name(PAYMENT_REFUND_RESULT)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic notificationTopic() {
    return TopicBuilder.name(NOTIFICATION)
        .partitions(3)
        .replicas(1)
        .build();
  }


}
