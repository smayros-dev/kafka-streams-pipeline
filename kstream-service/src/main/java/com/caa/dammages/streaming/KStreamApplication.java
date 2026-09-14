package com.caa.dammages.streaming;

import com.caa.dammages.streaming.config.KafkaStreamConfig;
import com.caa.dammages.streaming.service.AggregationService;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Main application for Kafka Streams sinistres processing.
 */
public final class KStreamApplication {

    /** Logger instance. */
    private static final Logger LOG =
            LoggerFactory.getLogger(KStreamApplication.class);

    /** Shutdown timeout in seconds. */
    private static final int SHUTDOWN_TIMEOUT = 10;

    /** Uncaught exception timeout in seconds. */
    private static final int ERROR_TIMEOUT = 5;

    private KStreamApplication() { }

    /**
     * Application entry point.
     *
     * @param args command line arguments
     * @throws InterruptedException if interrupted
     */
    public static void main(final String[] args)
            throws InterruptedException {
        final Properties config =
                KafkaStreamConfig.getProperties();
        final StreamsBuilder builder =
                new StreamsBuilder();

        final AggregationService service =
                new AggregationService();
        service.buildTopology(builder);

        final Topology topology = builder.build();
        LOG.info("Topologie Kafka Streams:\n{}",
                topology.describe());

        final KafkaStreams streams =
                new KafkaStreams(topology, config);

        streams.setUncaughtExceptionHandler(
                (thread, exception) -> {
            LOG.error("Exception non interceptee: {}",
                    exception.getMessage(), exception);
            streams.close(
                    Duration.ofSeconds(ERROR_TIMEOUT));
        });

        streams.setStateListener(
                (newState, oldState) -> {
            LOG.info("Changement d'etat: {} -> {}",
                    oldState, newState);
        });

        final CountDownLatch latch =
                new CountDownLatch(1);

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> {
            LOG.info("Arret du KStream...");
            streams.close(Duration.ofSeconds(
                    SHUTDOWN_TIMEOUT));
            LOG.info("KStream arrete proprement");
            latch.countDown();
        }));

        try {
            streams.start();
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Interruption: {}",
                    e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
