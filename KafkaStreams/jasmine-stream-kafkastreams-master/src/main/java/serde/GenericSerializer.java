package serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class GenericSerializer<T extends Serializable> implements Serializer<T> {

    private ObjectOutputStream outputStream;

    public GenericSerializer() {}

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public byte[] serialize(String topic, T t) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            outputStream.writeObject(t);
            outputStream.flush();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerializationException(e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public byte[] serialize(String topic, Headers headers, T t) {
        return this.serialize(topic, t);
    }
}