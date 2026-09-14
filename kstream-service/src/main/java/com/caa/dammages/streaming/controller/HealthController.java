package com.caa.dammages.streaming.controller;

import java.util.Map;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final KafkaStreams kafkaStreams;

    public HealthController(KafkaStreams kafkaStreams) {
        this.kafkaStreams = kafkaStreams;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean isRunning = kafkaStreams.state()
            .equals(KafkaStreams.State.RUNNING);

        Map<String, Object> response = Map.of(
            "status", isRunning ? "UP" : "DOWN",
            "state", kafkaStreams.state().toString()
        );

        return isRunning
            ? ResponseEntity.ok(response)
            : ResponseEntity.status(503).body(response);
    }
}
