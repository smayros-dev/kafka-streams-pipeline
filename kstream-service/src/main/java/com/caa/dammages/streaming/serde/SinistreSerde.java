package com.caa.dammages.streaming.serde;

import com.caa.dammages.streaming.model.Sinistre;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Serde for Sinistre model using Jackson.
 */
public final class SinistreSerde implements Serde<Sinistre> {

    /** Logger instance. */
    private static final Logger LOG =
            LoggerFactory.getLogger(SinistreSerde.class);

    /** Jackson mapper instance. */
    private final ObjectMapper mapper =
            new ObjectMapper();

    @Override
    public Serializer<Sinistre> serializer() {
        return (topic, data) -> {
            if (data == null) {
                return null;
            }
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                LOG.error("Erreur serialisation: {}",
                        e.getMessage());
                return null;
            }
        };
    }

    @Override
    public Deserializer<Sinistre> deserializer() {
        return (topic, bytes) -> {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(bytes,
                        Sinistre.class);
            } catch (Exception e) {
                LOG.error("Erreur deserialisation: {}",
                        e.getMessage());
                return null;
            }
        };
    }
}
