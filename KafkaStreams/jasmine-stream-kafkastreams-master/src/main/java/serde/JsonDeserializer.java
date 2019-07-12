package serde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import serde.JsonSerdable;

import java.util.Map;

public class JsonDeserializer<T extends JsonSerdable> implements Deserializer<T> {

    private ObjectMapper om;
    private TypeReference<T> tTypeReference;
    private boolean discardMalformedJson;

    public JsonDeserializer(TypeReference<T> tTypeReference, boolean discardMalformedJson) {
        this.tTypeReference = tTypeReference;
        this.discardMalformedJson = discardMalformedJson;
        this.om = new ObjectMapper();
    }

    @SuppressWarnings("unused")
    public JsonDeserializer() {
        this.om = new ObjectMapper();
        this.discardMalformedJson = false;
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public T deserialize(String topic, byte[] bytes) {
        T data;
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            data = T.deserialize(om, bytes, tTypeReference);
        } catch (Exception e) {
            if (this.discardMalformedJson) {
                return null;
            } else {
                throw new SerializationException(e);
            }
        }

        return data;
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        return this.deserialize(topic, data);
    }

    @Override
    public void close() {}
}
