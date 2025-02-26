package com.salas.emailnotificationservice.handlers;

import com.salas.common.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;

@Service
public class ProductEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductEventHandler.class);

    @KafkaListener(topicPartitions = @TopicPartition(topic = "product-crated-events-topic", partitions = { "0", "1", "2"}),
    containerFactory = "kafkaListenerContainerFactory")
    public void handler(ProductCreatedEvent createProductDto) {
        System.out.println(createProductDto);
        LOGGER.info("Receive event: {}", createProductDto.getTitle());
    }
}
