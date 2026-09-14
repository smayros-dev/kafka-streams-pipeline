package com.caa.dammages.streaming.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

/**
 * Kafka Streams configuration constants and properties.
 */
public final class KafkaStreamConfig {

    /** Application ID. */
    public static final String APPLICATION_ID =
            getEnvOrDefault("KSTREAM_APP_ID",
                    "sinistres-stream-app");

    /** Bootstrap servers. */
    public static final String BOOTSTRAP_SERVERS =
            getEnvOrDefault("KAFKA_BOOTSTRAP_SERVERS",
                    "localhost:9092");

    /** Raw sinistres topic. */
    public static final String TOPIC_RAW =
            getEnvOrDefault("TOPIC_RAW", "raw-sinistres");

    /** Critical sinistres topic. */
    public static final String TOPIC_CRITIQUES =
            getEnvOrDefault("TOPIC_CRITIQUES",
                    "sinistres-critiques");

    /** Stats topic. */
    public static final String TOPIC_STATS =
            getEnvOrDefault("TOPIC_STATS",
                    "stats-contrat-5m");

    /** Contracts reference topic. */
    public static final String TOPIC_CONTRATS =
            getEnvOrDefault("TOPIC_CONTRATS",
                    "contrats-ref");

    /** Enriched sinistres topic. */
    public static final String TOPIC_SINISTRES_ENRICHI =
            getEnvOrDefault("TOPIC_SINISTRES_ENRICHI",
                    "sinistres-enrichis");

    /** Critical threshold in EUR. */
    public static final double SEUIL_CRITIQUE =
            Double.parseDouble(
                    getEnvOrDefault("SEUIL_CRITIQUE",
                            "10000.0"));

    /** Window size in minutes. */
    public static final long WINDOW_SIZE_MINUTES =
            Long.parseLong(
                    getEnvOrDefault("WINDOW_SIZE_MINUTES",
                            "5"));

    /** Default commit interval in ms. */
    private static final int COMMIT_INTERVAL_MS = 1000;

    /** Default number of stream threads. */
    private static final int NUM_STREAM_THREADS = 1;

    private KafkaStreamConfig() { }

    /**
     * Get Kafka Streams properties.
     *
     * @return configured Properties
     */
    public static Properties getProperties() {
        final Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG,
                APPLICATION_ID);
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        config.put(
                StreamsConfig
                    .DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass());
        config.put(
                StreamsConfig
                    .DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");
        config.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG,
                NUM_STREAM_THREADS);
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
                COMMIT_INTERVAL_MS);
        return config;
    }

    /**
     * Get environment variable or default value.
     *
     * @param key environment variable key
     * @param defaultValue default value
     * @return value from env or default
     */
    private static String getEnvOrDefault(
            final String key, final String defaultValue) {
        final String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return defaultValue;
    }
}
