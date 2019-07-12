package serde;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import serde.JsonSerdable;

import java.util.Map;

public class JsonSerializer<T extends JsonSerdable> implements Serializer<T> {

    private ObjectMapper om;

    public JsonSerializer() {
        this.om = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String topic, T t) {
        byte[] bytes;
        try {
            bytes = t.serialize(om);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
        return bytes;
    }

    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        return this.serialize(topic, data);
    }

    @Override
    public void close() {

    }
}
