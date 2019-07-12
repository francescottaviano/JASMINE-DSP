package serde;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.Serializable;
import java.util.Map;

public class GenericSerde<T extends Serializable> implements Serde<T> {
    private GenericSerializer<T> serializer;
    private GenericDeserializer<T> deserializer;

    public GenericSerde() {
        this.serializer = new GenericSerializer<>();
        this.deserializer = new GenericDeserializer<>();
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        serializer.configure(map, b);
        deserializer.configure(map, b);
    }

    @Override
    public void close() {
        serializer.close();
        deserializer.close();
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
