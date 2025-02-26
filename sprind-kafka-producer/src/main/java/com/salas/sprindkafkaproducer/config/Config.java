package com.salas.sprindkafkaproducer.config;

import com.salas.common.ProductCreatedEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class Config {

    @Value("${spring.kafka.producer.key-serializer:default}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.bootstrap-servers}")
    private List<String> bootstrapServers;
    @Value("${spring.kafka.producer.acks}")
    private String acks;
    @Value("${spring.kafka.producer.properties.request.timeout.ms}")
    private String requestTimeout;
    @Value("${spring.kafka.producer.properties.linger.ms}")
    private String linger;

    @Value("${spring.kafka.producer.properties.enabled.idempotence}")
    private String idempotency;

    Map<String, Object> createConfig() {
        HashMap<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        configs.put(ProducerConfig.ACKS_CONFIG, acks);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        configs.put(ProducerConfig.LINGER_MS_CONFIG, linger);

        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, idempotency);
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return configs;
    }

    @Bean
    ProducerFactory<String, ProductCreatedEvent> producerFactory() {
        return new DefaultKafkaProducerFactory<String, ProductCreatedEvent>(createConfig());
    }

    @Bean
    KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    NewTopic newTopic() {
        return TopicBuilder
                .name("product-crated-events-topic")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }
}
