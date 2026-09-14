package com.caa.dammages.streaming.serde;

import com.caa.dammages.streaming.model.SinistreAggregator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Custom Serde for SinistreAggregator model.
 */
public final class SinistreAggregatorSerde
        implements Serde<SinistreAggregator> {

    /** Jackson mapper instance. */
    private final ObjectMapper mapper =
            new ObjectMapper();

    @Override
    public Serializer<SinistreAggregator> serializer() {
        return (topic, data) -> {
            if (data == null) {
                return null;
            }
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Erreur serialisation",
                        e);
            }
        };
    }

    @Override
    public Deserializer<SinistreAggregator> deserializer() {
        return (topic, bytes) -> {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(bytes,
                        SinistreAggregator.class);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Erreur deserialisation",
                        e);
            }
        };
    }
}
