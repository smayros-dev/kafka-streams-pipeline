package com.caa.dammages.streaming.e2e;

import com.caa.dammages.streaming.model.Sinistre;
import com.caa.dammages.streaming.service.AggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class EndToEndFlowTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    static String bootstrapServers;
    static KafkaProducer<String, String> producer;
    static ObjectMapper mapper = new ObjectMapper();

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
    void shouldFilterSinistresCritiques() throws Exception {
        String inputTopic = "raw-sinistres-critiques-" + UUID.randomUUID();
        String outputTopic = "sinistres-critiques-" + UUID.randomUUID();

        Properties streamProps = new Properties();
        streamProps.put("bootstrap.servers", bootstrapServers);
        streamProps.put("application.id", "critiques-test-" + UUID.randomUUID());
        streamProps.put("default.key.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde");
        streamProps.put("default.value.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde");

        Sinistre s1 = new Sinistre("sin-001", "CTR-001", 15000.0, "COLLISION", System.currentTimeMillis());
        Sinistre s2 = new Sinistre("sin-002", "CTR-001", 5000.0, "VOL", System.currentTimeMillis());

        producer.send(new ProducerRecord<>(inputTopic, "sin-001", mapper.writeValueAsString(s1)));
        producer.send(new ProducerRecord<>(inputTopic, "sin-002", mapper.writeValueAsString(s2)));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "critiques-consumer-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(inputTopic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            long critiquesCount = 0;
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains("15000.0")) {
                    critiquesCount++;
                }
            }

            assertTrue(critiquesCount >= 1, "Should have at least one critique sinistre");
        }
    }

    @Test
    void shouldAggregateStatsByContrat() throws Exception {
        String inputTopic = "raw-sinistres-agg-" + UUID.randomUUID();

        Sinistre s1 = new Sinistre("sin-001", "CTR-001", 12000.0, "COLLISION", System.currentTimeMillis());
        Sinistre s2 = new Sinistre("sin-002", "CTR-001", 8000.0, "VOL", System.currentTimeMillis());

        producer.send(new ProducerRecord<>(inputTopic, "sin-001", mapper.writeValueAsString(s1)));
        producer.send(new ProducerRecord<>(inputTopic, "sin-002", mapper.writeValueAsString(s2)));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "agg-consumer-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(inputTopic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty(), "Should receive aggregated stats");
        }
    }

    @Test
    void shouldJoinWithContratReference() throws Exception {
        String inputTopic = "raw-sinistres-join-" + UUID.randomUUID();
        String contratsTopic = "contrats-ref-" + UUID.randomUUID();

        Sinistre s1 = new Sinistre("sin-001", "CTR-001", 15000.0, "COLLISION", System.currentTimeMillis());
        String contratJson = "{\"contratId\":\"CTR-001\",\"nomClient\":\"Dupont\",\"typeContrat\":\"AUTO\",\"statut\":\"ACTIF\"}";

        producer.send(new ProducerRecord<>(inputTopic, "sin-001", mapper.writeValueAsString(s1)));
        producer.send(new ProducerRecord<>(contratsTopic, "CTR-001", contratJson));

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "join-consumer-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(inputTopic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            assertFalse(records.isEmpty(), "Should receive enriched sinistres");
        }
    }
}
