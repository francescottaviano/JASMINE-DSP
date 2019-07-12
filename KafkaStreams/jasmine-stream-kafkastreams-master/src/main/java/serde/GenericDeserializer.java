package serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;

public class GenericDeserializer<T extends Serializable> implements Deserializer<T> {

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] bytes) {
        try {
            if (bytes == null || bytes.length == 0) {
                return null;
            }

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream inputStream = new ObjectInputStream(in);

            return  (T) inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        }
    }

    public T deserialize(byte[] bytes) {
        return this.deserialize(null, bytes);
    }

    @Override
    public void close() {

    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        return this.deserialize(topic, data);
    }
}
