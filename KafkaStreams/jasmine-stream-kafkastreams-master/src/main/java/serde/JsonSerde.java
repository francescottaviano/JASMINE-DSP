package serde;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerde<T extends JsonSerdable> implements Serde<T> {

    private JsonSerializer<T> serializer;
    private JsonDeserializer<T> deserializer;

    public JsonSerde(TypeReference<T> typeReference, boolean discardMalformedJson) {
        this.init(typeReference, discardMalformedJson);
    }

    public JsonSerde(TypeReference<T> typeReference) {
        this.init(typeReference);
    }

    @SuppressWarnings("unused")
    public JsonSerde() {
        this.init();
    }

    private void init(TypeReference<T> typeReference, boolean discardMalformedJson) {
        this.serializer = new JsonSerializer<>();
        this.deserializer = new JsonDeserializer<>(typeReference, discardMalformedJson);
    }

    private void init() {
        this.init(null, false);
    }

    private void init(TypeReference<T> typeReference) {
        this.init(typeReference, false);
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {}

    @Override
    public void close() {
        this.serializer.close();
        this.deserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return this.serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this.deserializer;
    }
}
