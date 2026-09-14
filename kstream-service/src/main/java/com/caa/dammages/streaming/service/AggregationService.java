package com.caa.dammages.streaming.service;

import com.caa.dammages.streaming.config.KafkaStreamConfig;
import com.caa.dammages.streaming.model.Sinistre;
import com.caa.dammages.streaming.model.SinistreAggregator;
import com.caa.dammages.streaming.serde.SinistreAggregatorSerde;
import com.caa.dammages.streaming.serde.SinistreSerde;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service that builds Kafka Streams topology.
 */
public final class AggregationService {

    /** Logger instance. */
    private static final Logger LOG =
            LoggerFactory.getLogger(AggregationService.class);

    /** JSON mapper instance. */
    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    private final SinistreSerde sinistreSerde;

    /** Default constructor. */
    public AggregationService() {
        this.sinistreSerde = new SinistreSerde();
    }

    /**
     * Build the complete Kafka Streams topology.
     *
     * @param builder StreamsBuilder to configure
     */
    public void buildTopology(final StreamsBuilder builder) {
        final KStream<String, String> rawSinistres =
                builder.stream(
                        KafkaStreamConfig.TOPIC_RAW,
                        Consumed.with(
                                Serdes.String(),
                                Serdes.String()));

        final GlobalKTable<String, String> contratsTable =
                builder.globalTable(
                        KafkaStreamConfig.TOPIC_CONTRATS,
                        Consumed.with(
                                Serdes.String(),
                                Serdes.String()));

        buildKStreamFilterBranch(rawSinistres);
        buildKTableAggregationBranch(rawSinistres);
        buildKGlobalTableJoinBranch(
                rawSinistres, contratsTable);

        LOG.info("Topologie Kafka Streams construite");
    }

    /**
     * Parse JSON value to Sinistre object.
     *
     * @param jsonValue JSON string
     * @return parsed Sinistre or null
     */
    private Sinistre parseSinistre(final String jsonValue) {
        if (jsonValue == null || jsonValue.isEmpty()) {
            return null;
        }
        try {
            return sinistreSerde.deserializer()
                    .deserialize(
                            KafkaStreamConfig.TOPIC_RAW,
                            jsonValue.getBytes());
        } catch (Exception e) {
            LOG.error("Erreur deserialisation: {}",
                    e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert map to JSON string.
     *
     * @param map data map
     * @return JSON string or null
     */
    private String toJson(final Map<String, Object> map) {
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            LOG.error("Erreur serialisation: {}",
                    e.getMessage(), e);
            return null;
        }
    }

    /**
     * Build KStream branch for filtering critical claims.
     *
     * @param rawSinistres source stream
     */
    private void buildKStreamFilterBranch(
            final KStream<String, String> rawSinistres) {
        rawSinistres
            .filter((key, jsonValue) -> {
                final Sinistre s = parseSinistre(jsonValue);
                return s != null
                        && s.getMontantSinistre()
                        > KafkaStreamConfig.SEUIL_CRITIQUE;
            })
            .mapValues((String jsonValue) -> {
                final Sinistre s = parseSinistre(jsonValue);
                if (s == null) {
                    return (String) null;
                }
                final Map<String, Object> jsonMap =
                        new HashMap<>();
                jsonMap.put("sinistreId",
                        s.getSinistreId());
                jsonMap.put("contratId",
                        s.getContratId());
                jsonMap.put("montantSinistre",
                        s.getMontantSinistre());
                jsonMap.put("typeSinistre",
                        s.getTypeSinistre());
                jsonMap.put("dateDeclaration",
                        s.getDateDeclaration());
                jsonMap.put("seuilDepasse",
                        KafkaStreamConfig.SEUIL_CRITIQUE);
                return toJson(jsonMap);
            })
            .filter((key, value) -> value != null)
            .to(KafkaStreamConfig.TOPIC_CRITIQUES,
                    Produced.with(Serdes.String(),
                            Serdes.String()));

        LOG.info("Branche KStream initialisee");
    }

    /**
     * Build KTable branch for aggregation by contract.
     *
     * @param rawSinistres source stream
     */
    private void buildKTableAggregationBranch(
            final KStream<String, String> rawSinistres) {
        final KTable<Windowed<String>,
                SinistreAggregator> byContrat =
                rawSinistres
            .filter((key, jsonValue) -> {
                final Sinistre s = parseSinistre(jsonValue);
                return s != null
                        && s.getContratId() != null
                        && !s.getContratId().isEmpty();
            })
            .mapValues(
                    (String jsonValue) ->
                            parseSinistre(jsonValue))
            .filter((key, sinistre) -> sinistre != null)
            .map((key, sinistre) ->
                    new KeyValue<>(
                            sinistre.getContratId(),
                            sinistre))
            .groupByKey(Grouped.with(
                    Serdes.String(), sinistreSerde))
            .windowedBy(
                    TimeWindows.ofSizeWithNoGrace(
                            Duration.ofMinutes(5)))
            .aggregate(
                SinistreAggregator::new,
                (cid, sinistre, agg) -> {
                    final SinistreAggregator na =
                            new SinistreAggregator();
                    na.setTotalMontant(
                            agg.getTotalMontant()
                            + sinistre
                                .getMontantSinistre());
                    na.setNbSinistres(
                            agg.getNbSinistres() + 1);
                    return na;
                },
                Materialized.<String,
                        SinistreAggregator,
                        WindowStore<Bytes, byte[]>>
                    as("sinistres-by-contrat-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(
                            new SinistreAggregatorSerde())
            );

        byContrat
            .toStream()
            .map((windowedKey, agg) -> {
                agg.setWindowStart(
                        windowedKey.window().start());
                agg.setWindowEnd(
                        windowedKey.window().end());
                final Map<String, Object> jsonMap =
                        new HashMap<>();
                jsonMap.put("contratId",
                        windowedKey.key());
                jsonMap.put("totalMontant",
                        agg.getTotalMontant());
                jsonMap.put("nbSinistres",
                        agg.getNbSinistres());
                jsonMap.put("windowStart",
                        agg.getWindowStart());
                jsonMap.put("windowEnd",
                        agg.getWindowEnd());
                return new KeyValue<>(
                        windowedKey.key(),
                        toJson(jsonMap));
            })
            .filter((key, value) -> value != null)
            .to(KafkaStreamConfig.TOPIC_STATS,
                    Produced.with(Serdes.String(),
                            Serdes.String()));

        LOG.info("Branche KTable initialisee");
    }

    /**
     * Build KGlobalTable branch for contract join.
     *
     * @param rawSinistres source stream
     * @param contratsTable global reference table
     */
    private void buildKGlobalTableJoinBranch(
            final KStream<String, String> rawSinistres,
            final GlobalKTable<String,
                    String> contratsTable) {
        rawSinistres
            .filter((key, jsonValue) -> {
                final Sinistre s = parseSinistre(jsonValue);
                return s != null
                        && s.getContratId() != null
                        && !s.getContratId().isEmpty();
            })
            .mapValues(
                    (String jsonValue) ->
                            parseSinistre(jsonValue))
            .filter((key, sinistre) -> sinistre != null)
            .selectKey(
                    (key, sinistre) ->
                            sinistre.getContratId())
            .leftJoin(
                contratsTable,
                (key, sinistre) -> key,
                (sinistre, contratJson) -> {
                    final Map<String, Object> joined =
                            new HashMap<>();
                    joined.put("sinistreId",
                            sinistre.getSinistreId());
                    joined.put("contratId",
                            sinistre.getContratId());
                    joined.put("montantSinistre",
                            sinistre.getMontantSinistre());
                    joined.put("typeSinistre",
                            sinistre.getTypeSinistre());
                    joined.put("dateDeclaration",
                            sinistre.getDateDeclaration());
                    if (contratJson != null) {
                        joined.put("contratInfo",
                                contratJson);
                    } else {
                        joined.put("contratInfo",
                                "CONTRAT_INCONNU");
                    }
                    return toJson(joined);
                })
            .filter((key, value) -> value != null)
            .to(KafkaStreamConfig.TOPIC_SINISTRES_ENRICHI,
                    Produced.with(Serdes.String(),
                            Serdes.String()));

        LOG.info("Branche KGlobalTable initialisee");
    }
}
