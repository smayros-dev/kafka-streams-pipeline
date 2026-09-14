package com.caa.dammages.streaming;

import com.caa.dammages.streaming.config.KafkaStreamConfig;
import com.caa.dammages.streaming.model.Sinistre;
import com.caa.dammages.streaming.serde.SinistreSerde;
import com.caa.dammages.streaming.service.AggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class KStreamApplicationTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, String> inputTopic;
    private TestInputTopic<String, String> contratsTopic;
    private TestOutputTopic<String, String> critiquesTopic;
    private TestOutputTopic<String, String> statsTopic;
    private TestOutputTopic<String, String> enrichisTopic;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Properties props = KafkaStreamConfig.getProperties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        StreamsBuilder builder = new StreamsBuilder();

        AggregationService aggregationService = new AggregationService();
        aggregationService.buildTopology(builder);

        testDriver = new TopologyTestDriver(builder.build(), props);
        inputTopic = testDriver.createInputTopic(KafkaStreamConfig.TOPIC_RAW, Serdes.String().serializer(), Serdes.String().serializer());
        contratsTopic = testDriver.createInputTopic(KafkaStreamConfig.TOPIC_CONTRATS, Serdes.String().serializer(), Serdes.String().serializer());
        critiquesTopic = testDriver.createOutputTopic(KafkaStreamConfig.TOPIC_CRITIQUES, Serdes.String().deserializer(), Serdes.String().deserializer());
        statsTopic = testDriver.createOutputTopic(KafkaStreamConfig.TOPIC_STATS, Serdes.String().deserializer(), Serdes.String().deserializer());
        enrichisTopic = testDriver.createOutputTopic(KafkaStreamConfig.TOPIC_SINISTRES_ENRICHI, Serdes.String().deserializer(), Serdes.String().deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldRouteSinistreCritique() throws IOException {
        Sinistre s = new Sinistre("sin-001", "CTR-001", 15000.0, "COLLISION", System.currentTimeMillis());
        inputTopic.pipeInput("sin-001", mapper.writeValueAsString(s));

        assertFalse(critiquesTopic.isEmpty());
        String result = critiquesTopic.readValue();
        assertTrue(result.contains("CTR-001"));
        assertTrue(result.contains("15000"));
    }

    @Test
    void shouldNotRouteSinistreBelowThreshold() throws IOException {
        Sinistre s = new Sinistre("sin-002", "CTR-002", 5000.0, "VOL", System.currentTimeMillis());
        inputTopic.pipeInput("sin-002", mapper.writeValueAsString(s));

        assertTrue(critiquesTopic.isEmpty());
    }

    @Test
    void shouldHandleNullJsonValue() {
        inputTopic.pipeInput("sin-null", (String) null);
        assertTrue(critiquesTopic.isEmpty());
    }

    @Test
    void shouldHandleMalformedJson() {
        inputTopic.pipeInput("sin-bad", "not a json");
        assertTrue(critiquesTopic.isEmpty());
    }

    @Test
    void shouldFilterByContratId() throws IOException {
        Sinistre s1 = new Sinistre("sin-003", "CTR-003", 12000.0, "INCENDIE", System.currentTimeMillis());
        Sinistre s2 = new Sinistre("sin-004", "CTR-003", 8000.0, "VOL", System.currentTimeMillis());

        inputTopic.pipeInput("sin-003", mapper.writeValueAsString(s1));
        inputTopic.pipeInput("sin-004", mapper.writeValueAsString(s2));

        assertFalse(critiquesTopic.isEmpty());
    }

    @Test
    void shouldAggregateStatsByContrat() throws IOException {
        Sinistre s1 = new Sinistre("sin-005", "CTR-005", 12000.0, "COLLISION", System.currentTimeMillis());
        Sinistre s2 = new Sinistre("sin-006", "CTR-005", 8000.0, "VOL", System.currentTimeMillis());

        inputTopic.pipeInput("sin-005", mapper.writeValueAsString(s1));
        inputTopic.pipeInput("sin-006", mapper.writeValueAsString(s2));

        assertFalse(statsTopic.isEmpty());
        String result = statsTopic.readValue();
        assertTrue(result.contains("CTR-005"));
        assertTrue(result.contains("totalMontant"));
        assertTrue(result.contains("nbSinistres"));
    }

    @Test
    void shouldJoinWithContratReference() throws IOException {
        String contratJson = "{\"contratId\":\"CTR-007\",\"nomClient\":\"Dupont\",\"typeContrat\":\"AUTO\",\"statut\":\"ACTIF\"}";
        contratsTopic.pipeInput("CTR-007", contratJson);

        Sinistre s = new Sinistre("sin-007", "CTR-007", 15000.0, "COLLISION", System.currentTimeMillis());
        inputTopic.pipeInput("sin-007", mapper.writeValueAsString(s));

        assertFalse(enrichisTopic.isEmpty());
        String result = enrichisTopic.readValue();
        assertTrue(result.contains("CTR-007"));
        assertTrue(result.contains("Dupont"));
        assertTrue(result.contains("COLLISION"));
    }

    @Test
    void shouldHandleMissingContratGracefully() throws IOException {
        Sinistre s = new Sinistre("sin-008", "CTR-UNKNOWN", 15000.0, "VOL", System.currentTimeMillis());
        inputTopic.pipeInput("sin-008", mapper.writeValueAsString(s));

        assertFalse(enrichisTopic.isEmpty());
        String result = enrichisTopic.readValue();
        assertTrue(result.contains("CTR-UNKNOWN"));
        assertTrue(result.contains("CONTRAT_INCONNU"));
    }
}
