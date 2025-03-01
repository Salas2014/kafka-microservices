package com.salas.emailnotificationservice.handlers;

import com.salas.common.ProductCreatedEvent;
import com.salas.emailnotificationservice.exseption.NonRetryebleExeception;
import com.salas.emailnotificationservice.exseption.RetryebleExeception;
import com.salas.emailnotificationservice.percistence.ProcessedEventEntity;
import com.salas.emailnotificationservice.percistence.ProcessedEventsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class ProductEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductEventHandler.class);
    private final RestTemplate restTemplate;
    private final ProcessedEventsRepository processedEventsRepository;

    public ProductEventHandler(RestTemplate restTemplate, ProcessedEventsRepository processedEventsRepository) {
        this.restTemplate = restTemplate;
        this.processedEventsRepository = processedEventsRepository;
    }

    @Transactional
    @KafkaListener(topics = "product-crated-events-topic", containerFactory = "kafkaListenerContainerFactory")
    public void handler(@Payload ProductCreatedEvent createProductEvent,
                        @Header("messageId") String messageId,
                        @Header(KafkaHeaders.RECEIVED_KEY) String messageKey) {

        Optional<ProcessedEventEntity> eventEntity = processedEventsRepository.findByMessageId(messageId);

        if (eventEntity.isPresent()) {
            LOGGER.info("Duplicate message id: {}", messageId);
            return;
        }

        try {
            String url = "http://localhost:9090/responce/200";
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (exchange.getStatusCode().value() == HttpStatus.OK.value()) {
                System.out.println(createProductEvent);
                LOGGER.info("Receive event: {}", exchange.getBody());
            }
        } catch (ResourceAccessException resourceAccessException) {
            LOGGER.error(resourceAccessException.getMessage());
            throw new RetryebleExeception(resourceAccessException);
        } catch (HttpServerErrorException httpServerErrorException) {
            LOGGER.error(httpServerErrorException.getMessage());
            throw new NonRetryebleExeception(httpServerErrorException);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new NonRetryebleExeception(e);
        }

        try {
            ProcessedEventEntity save = processedEventsRepository.save(new ProcessedEventEntity(messageKey, createProductEvent.getProductid()));
            LOGGER.info("Product: {}", save);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error(e.getMessage());
            throw new NonRetryebleExeception(e);
        }
    }
}
