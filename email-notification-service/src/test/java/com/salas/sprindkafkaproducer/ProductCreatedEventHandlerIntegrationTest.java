package com.salas.sprindkafkaproducer;

import com.salas.common.ProductCreatedEvent;
import com.salas.sprindkafkaproducer.handlers.ProductEventHandler;
import com.salas.sprindkafkaproducer.percistence.ProcessedEventEntity;
import com.salas.sprindkafkaproducer.percistence.ProcessedEventsRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("standalone")
@EmbeddedKafka
@SpringBootTest(
        properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}"
)
public class ProductCreatedEventHandlerIntegrationTest {

    @MockitoBean
    private ProcessedEventsRepository processedEventsRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoSpyBean
    private ProductEventHandler productEventHandler;

    @Test
    public void testProductCreatedEventHandler_OnProductCreated_HandlesEvent() throws ExecutionException, InterruptedException {
        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        productCreatedEvent.setPrice("100");
        productCreatedEvent.setProductid(UUID.randomUUID().toString());
        productCreatedEvent.setQuantity("3");
        productCreatedEvent.setTitle("Test Title");

        String messageId = UUID.randomUUID().toString();
        String messageKey = productCreatedEvent.getProductid();

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "product-crated-events-topic", messageKey, productCreatedEvent
        );

        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        ProcessedEventEntity processedEventEntity = new ProcessedEventEntity();
        when(processedEventsRepository.findByMessageId(anyString())).thenReturn(Optional.of(processedEventEntity));
        when(processedEventsRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);


        String responseBody = "{\"key\":\"value\"}";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, httpHeaders, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class), any(HttpMethod.class),
                isNull(), eq(String.class))).thenReturn(responseEntity);


        kafkaTemplate.send(record).get();

        var messageIdCaptor = ArgumentCaptor.forClass(String.class);
        var messageKeyCaptor = ArgumentCaptor.forClass(String.class);
        var eventArgumentCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);

        verify(productEventHandler, timeout(5000).times(1)).handler(
                eventArgumentCaptor.capture(), messageIdCaptor.capture(), messageKeyCaptor.capture());

        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());
    }
}




