package com.salas.sprindkafkaproducer;

import com.salas.common.ProductCreatedEvent;
import com.salas.sprindkafkaproducer.service.ProductService;
import com.salas.sprindkafkaproducer.service.dto.CreateProductDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
@SpringBootTest(
        properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private Environment environment;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaMessageListenerContainer<String, ProductCreatedEvent> container;
    private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>> records;

    @BeforeAll
    void setUp() {
        var handlingDeserializer = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ProductCreatedEvent.class));

        DefaultKafkaConsumerFactory<String, ProductCreatedEvent> defaultKafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(
                getConsumerProperties(), new StringDeserializer(), handlingDeserializer
        );

        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("created.events.topic.name"));

        container = new KafkaMessageListenerContainer<>(defaultKafkaConsumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @Test
    void testCreateProduct_whenValidProductDetails_successfullySendKafkaMessage() throws ExecutionException, InterruptedException {

        String title = "Samsung";
        String price = "500";
        String quantity = "10";

        CreateProductDto createProductDto = new CreateProductDto(
                title, price, quantity
        );

        productService.createProduct(createProductDto);

        ConsumerRecord<String, ProductCreatedEvent> consumerRecord = records.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(consumerRecord);
        assertNotNull(consumerRecord.key());

        ProductCreatedEvent value = consumerRecord.value();
        assertEquals(title, value.getTitle());
        assertEquals(price, value.getPrice());
        assertEquals(quantity, value.getQuantity());
    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

    private Map<String, Object> getConsumerProperties() {

        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.group-id")),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.auto-offset-reset")),
                JsonDeserializer.TRUSTED_PACKAGES, Objects.requireNonNull(environment.getProperty("spring.kafka.consumer.properties.json.trusted.packages"))
        );
    }
}
