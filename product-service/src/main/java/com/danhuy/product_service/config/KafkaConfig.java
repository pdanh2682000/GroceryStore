package com.danhuy.product_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

  @Value("${kafka.topics.product-created}")
  private String productCreatedTopic;

  @Value("${kafka.topics.product-updated}")
  private String productUpdatedTopic;

  @Value("${kafka.topics.product-deleted}")
  private String productDeletedTopic;

  @Bean
  public NewTopic productCreatedTopic() {
    return TopicBuilder.name(productCreatedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic productUpdatedTopic() {
    return TopicBuilder.name(productUpdatedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic productDeletedTopic() {
    return TopicBuilder.name(productDeletedTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

}
