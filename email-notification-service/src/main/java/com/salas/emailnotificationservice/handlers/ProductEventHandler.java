package com.salas.emailnotificationservice.handlers;

import com.salas.common.ProductCreatedEvent;
import com.salas.emailnotificationservice.exseption.NonRetryebleExeception;
import com.salas.emailnotificationservice.exseption.RetryebleExeception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductEventHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(ProductEventHandler.class);

    private final RestTemplate restTemplate;

    public ProductEventHandler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "product-crated-events-topic", partitions = {"0", "1", "2"}),
            containerFactory = "kafkaListenerContainerFactory")
    public void handler(ProductCreatedEvent createProductDto) {


        try {
            String url = "http://localhost:9090/responce/200";
            ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            if (exchange.getStatusCode().value() == HttpStatus.OK.value()) {
                System.out.println(createProductDto);
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


    }
}
