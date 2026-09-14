package com.caa.dammages.streaming.acceptance;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class KafkaStreamAcceptanceTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static String bootstrapServers;
    static KafkaProducer<String, String> producer;

    @BeforeAll
    static void setUp() {
        bootstrapServers = kafka.getBootstrapServers();

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProps);
    }

    @AfterAll
    static void tearDown() {
        if (producer != null) {
            producer.close();
        }
    }

    @Test
    void shouldProduceAndConsumeSinistre() {
        String topic = "raw-sinistres-" + UUID.randomUUID();
        String sinistreJson = "{\"sinistreId\":\"sin-001\",\"contratId\":\"CTR-001\",\"montantSinistre\":15000.0,\"typeSinistre\":\"COLLISION\",\"dateDeclaration\":" + System.currentTimeMillis() + "}";

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, "sin-001", sinistreJson);
        producer.send(record);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty(), "Should receive at least one record");
            ConsumerRecord<String, String> received = records.iterator().next();
            assertEquals("sin-001", received.key());
            assertTrue(received.value().contains("CTR-001"));
        }
    }

    @Test
    void shouldProduceMultipleSinistres() {
        String topic = "raw-sinistres-multi-" + UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            String sinistreJson = "{\"sinistreId\":\"sin-" + i + "\",\"contratId\":\"CTR-001\",\"montantSinistre\":" + (10000 + i * 1000) + ".0,\"typeSinistre\":\"COLLISION\",\"dateDeclaration\":" + System.currentTimeMillis() + "}";
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, "sin-" + i, sinistreJson);
            producer.send(record);
        }

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-multi-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            assertTrue(records.count() >= 5, "Should receive at least 5 records");
        }
    }
}
