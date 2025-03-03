package com.salas.sprindkafkaproducer.service;

import com.salas.common.ProductCreatedEvent;
import com.salas.sprindkafkaproducer.service.dto.CreateProductDto;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService implements IProductService {
    private final static String TOPIC_NAME = "product-crated-events-topic";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductDto dto) throws ExecutionException, InterruptedException {
        // TODO save to db
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                productId, dto.getTitle(), dto.getPrice(), dto.getQuantity());

        var record = new ProducerRecord<>(TOPIC_NAME, productId, productCreatedEvent);
        record.headers().add("messageId", UUID.randomUUID().toString().getBytes());

        SendResult<String, ProductCreatedEvent> event = kafkaTemplate.send(record).get();

        LOGGER.info("Message sent successfully: {}", event.getRecordMetadata());
        LOGGER.info("Topic: {}", event.getRecordMetadata().topic());
        LOGGER.info("Partition: {}", event.getRecordMetadata().partition());
        LOGGER.info("Offset: {}", event.getRecordMetadata().offset());


        LOGGER.info("Result: {}", productId);
        return productId;
    }
}
